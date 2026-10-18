-- =========================================================
-- CAMPUS EXCHANGE - PRODUCTION-GRADE POSTGRESQL MIGRATION
-- Migration Version: 3.3 (Audited & Security Hardened)
-- Copy & Run this script in Supabase Dashboard -> SQL Editor
-- =========================================================

-- =========================================================
-- 1. TABLES & COLUMNS (Safe Creation & Extension)
-- =========================================================

-- PROFILES TABLE
create table if not exists public.profiles (
  id uuid references auth.users on delete cascade primary key,
  full_name text not null,
  course text default 'Computer Science • 3rd Year',
  phone text default '',
  bio text default '',
  avatar_url text default '',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Safely add missing profile columns if table already existed
alter table public.profiles add column if not exists course text default 'Computer Science • 3rd Year';
alter table public.profiles add column if not exists phone text default '';
alter table public.profiles add column if not exists bio text default '';
alter table public.profiles add column if not exists avatar_url text default '';

-- PUBLIC PROFILES VIEW (Exposes non-sensitive profile info to other students for cards/search)
create or replace view public.public_profiles as
select id, full_name, course, avatar_url
from public.profiles;

grant select on public.public_profiles to authenticated, anon;

-- ITEMS (MARKETPLACE) TABLE
create table if not exists public.items (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  price text not null,
  category text not null,
  location text not null,
  image_url text default '',
  lending_type text default 'Free', -- Free, Fixed Price, Rent on Duration
  duration text default '',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- BORROW REQUESTS TABLE
create table if not exists public.borrow_requests (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete cascade not null,
  requester_id uuid references public.profiles(id) on delete cascade not null,
  owner_id uuid references public.profiles(id) on delete cascade not null,
  status text default 'Pending',
  message text default '',
  duration text default '7 Days',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Safely add missing columns if borrow_requests already existed
alter table public.borrow_requests add column if not exists duration text default '7 Days';

-- Safely attach constraints to borrow_requests
do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'check_different_borrow_users') then
    alter table public.borrow_requests add constraint check_different_borrow_users check (requester_id <> owner_id);
  end if;
  if not exists (select 1 from pg_constraint where conname = 'check_borrow_request_status') then
    alter table public.borrow_requests add constraint check_borrow_request_status check (status in ('Pending', 'Accepted', 'Rejected', 'Returned', 'Cancelled'));
  end if;
exception
  when others then
    raise notice 'Notice: Some borrow_requests constraints could not be attached due to existing incompatible rows. Please review manually: %', sqlerrm;
end;
$$;

-- LOST & FOUND REPORTS TABLE
create table if not exists public.lost_found_reports (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  is_lost boolean not null, -- true = Lost, false = Found
  category text not null,
  location text not null,
  description text default '',
  status text default 'Active',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Safely attach constraints to lost_found_reports
do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'check_lost_found_status') then
    alter table public.lost_found_reports add constraint check_lost_found_status check (status in ('Active', 'Resolved'));
  end if;
exception
  when others then
    raise notice 'Notice: Could not attach lost_found_status constraint: %', sqlerrm;
end;
$$;

-- CHATS (CONVERSATIONS) TABLE
create table if not exists public.chats (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete set null,
  lost_found_id uuid references public.lost_found_reports(id) on delete set null,
  participant_one uuid references public.profiles(id) on delete cascade not null,
  participant_two uuid references public.profiles(id) on delete cascade not null,
  workflow_type text default 'BORROW_LEND',
  last_message text default '',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Safely add missing chat columns if chats already existed
alter table public.chats add column if not exists lost_found_id uuid references public.lost_found_reports(id) on delete set null;
alter table public.chats add column if not exists workflow_type text default 'BORROW_LEND';

-- Safely attach constraints to chats
do $$
begin
  if not exists (select 1 from pg_constraint where conname = 'check_chat_participants') then
    alter table public.chats add constraint check_chat_participants check (participant_one <> participant_two);
  end if;
  if not exists (select 1 from pg_constraint where conname = 'check_chat_workflow_integrity') then
    alter table public.chats add constraint check_chat_workflow_integrity check (
      (workflow_type = 'BORROW_LEND' and item_id is not null and lost_found_id is null) or
      (workflow_type = 'LOST_FOUND' and lost_found_id is not null and item_id is null)
    );
  end if;
exception
  when others then
    raise notice 'Notice: Could not attach chat constraints due to existing incompatible rows: %', sqlerrm;
end;
$$;

-- MESSAGES TABLE
create table if not exists public.messages (
  id uuid default gen_random_uuid() primary key,
  chat_id uuid references public.chats(id) on delete cascade not null,
  sender_id uuid references public.profiles(id) on delete cascade not null,
  text text not null check (length(trim(text)) > 0),
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- =========================================================
-- 2. INDEXES
-- =========================================================

create index if not exists idx_items_user_id on public.items(user_id);
create index if not exists idx_items_category on public.items(category);
create index if not exists idx_borrow_requests_item_id on public.borrow_requests(item_id);
create index if not exists idx_borrow_requests_participants on public.borrow_requests(requester_id, owner_id);
create index if not exists idx_lost_found_user_id on public.lost_found_reports(user_id);
create index if not exists idx_lost_found_status on public.lost_found_reports(status);
create index if not exists idx_chats_participants on public.chats(participant_one, participant_two);
create index if not exists idx_messages_chat_id_created on public.messages(chat_id, created_at);

-- =========================================================
-- 3. HARDENED SECURITY DEFINER FUNCTIONS & TRIGGERS
-- =========================================================

-- BORROW REQUEST UPDATE VALIDATION
create or replace function public.check_borrow_request_update()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
begin
  if new.item_id <> old.item_id or new.requester_id <> old.requester_id or new.owner_id <> old.owner_id then
    raise exception 'Cannot modify item_id, requester_id, or owner_id on an existing request';
  end if;

  if auth.uid() = old.requester_id then
    if old.status = 'Pending' and new.status = 'Cancelled' then
      return new;
    else
      raise exception 'Requester can only cancel a pending borrow request';
    end if;
  elsif auth.uid() = old.owner_id then
    if old.status = 'Pending' and new.status in ('Accepted', 'Rejected') then
      return new;
    elsif old.status = 'Accepted' and new.status = 'Returned' then
      return new;
    else
      raise exception 'Invalid status transition for item owner';
    end if;
  else
    raise exception 'Not authorized to update this borrow request';
  end if;
end;
$$;

revoke all on function public.check_borrow_request_update() from public;
grant execute on function public.check_borrow_request_update() to authenticated, service_role;

drop trigger if exists trigger_check_borrow_request_update on public.borrow_requests;
create trigger trigger_check_borrow_request_update
  before update on public.borrow_requests
  for each row execute procedure public.check_borrow_request_update();

-- CHAT IMMUTABILITY TRIGGER
create or replace function public.check_chat_update()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
begin
  if new.participant_one <> old.participant_one or new.participant_two <> old.participant_two
     or new.workflow_type <> old.workflow_type or new.item_id is distinct from old.item_id
     or new.lost_found_id is distinct from old.lost_found_id then
    raise exception 'Cannot modify chat participants or related item/report references';
  end if;
  return new;
end;
$$;

revoke all on function public.check_chat_update() from public;
grant execute on function public.check_chat_update() to authenticated, service_role;

drop trigger if exists trigger_check_chat_update on public.chats;
create trigger trigger_check_chat_update
  before update on public.chats
  for each row execute procedure public.check_chat_update();

-- AUTOMATIC PROFILE CREATION TRIGGER
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
begin
  insert into public.profiles (id, full_name, course, phone, bio, avatar_url)
  values (
    new.id,
    coalesce(nullif(trim(new.raw_user_meta_data->>'full_name'), ''), split_part(new.email, '@', 1), 'Student'),
    'Computer Science • 3rd Year',
    '',
    '',
    ''
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

revoke all on function public.handle_new_user() from public;
grant execute on function public.handle_new_user() to service_role;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- =========================================================
-- 4. ROW LEVEL SECURITY (RLS) POLICIES
-- =========================================================

alter table public.profiles enable row level security;
alter table public.items enable row level security;
alter table public.borrow_requests enable row level security;
alter table public.lost_found_reports enable row level security;
alter table public.chats enable row level security;
alter table public.messages enable row level security;

-- PROFILES RLS (Base Table Protection)
-- Base table allows a user to read, insert, and update their OWN profile row.
-- Other students access non-sensitive info via public.public_profiles view.
drop policy if exists "Allow public read access to profiles" on public.profiles;
drop policy if exists "Allow users to read their own full profile" on public.profiles;
create policy "Allow users to read their own full profile"
  on public.profiles for select
  to authenticated
  using (auth.uid() = id);

drop policy if exists "Allow users to insert their own profile" on public.profiles;
create policy "Allow users to insert their own profile"
  on public.profiles for insert
  to authenticated
  with check (auth.uid() = id);

drop policy if exists "Allow users to update their own profile" on public.profiles;
create policy "Allow users to update their own profile"
  on public.profiles for update
  to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

-- ITEMS RLS
drop policy if exists "Allow public read access to items" on public.items;
create policy "Allow public read access to items"
  on public.items for select
  to authenticated
  using (true);

drop policy if exists "Allow authenticated users to insert items" on public.items;
create policy "Allow authenticated users to insert items"
  on public.items for insert
  to authenticated
  with check (auth.uid() = user_id);

drop policy if exists "Allow owners to update their items" on public.items;
create policy "Allow owners to update their items"
  on public.items for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists "Allow owners to delete their items" on public.items;
create policy "Allow owners to delete their items"
  on public.items for delete
  to authenticated
  using (auth.uid() = user_id);

-- BORROW REQUESTS RLS
drop policy if exists "Allow participants to read borrow requests" on public.borrow_requests;
create policy "Allow participants to read borrow requests"
  on public.borrow_requests for select
  to authenticated
  using (auth.uid() = requester_id or auth.uid() = owner_id);

drop policy if exists "Allow requesters to insert borrow requests" on public.borrow_requests;
create policy "Allow requesters to insert borrow requests"
  on public.borrow_requests for insert
  to authenticated
  with check (
    auth.uid() = requester_id
    and requester_id <> owner_id
    and owner_id = (select user_id from public.items where id = item_id)
  );

drop policy if exists "Allow participants to update borrow requests" on public.borrow_requests;
create policy "Allow participants to update borrow requests"
  on public.borrow_requests for update
  to authenticated
  using (auth.uid() = requester_id or auth.uid() = owner_id);

-- LOST & FOUND REPORTS RLS
drop policy if exists "Allow public read access to lost found reports" on public.lost_found_reports;
create policy "Allow public read access to lost found reports"
  on public.lost_found_reports for select
  to authenticated
  using (true);

drop policy if exists "Allow authenticated users to insert lost found reports" on public.lost_found_reports;
create policy "Allow authenticated users to insert lost found reports"
  on public.lost_found_reports for insert
  to authenticated
  with check (auth.uid() = user_id);

drop policy if exists "Allow reporters to update lost found reports" on public.lost_found_reports;
create policy "Allow reporters to update lost found reports"
  on public.lost_found_reports for update
  to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

-- CHATS RLS
drop policy if exists "Allow participants to read chats" on public.chats;
create policy "Allow participants to read chats"
  on public.chats for select
  to authenticated
  using (auth.uid() = participant_one or auth.uid() = participant_two);

drop policy if exists "Allow participants to insert chats" on public.chats;
create policy "Allow participants to insert chats"
  on public.chats for insert
  to authenticated
  with check (
    (auth.uid() = participant_one or auth.uid() = participant_two)
    and participant_one <> participant_two
    and (
      (workflow_type = 'BORROW_LEND' and item_id is not null and lost_found_id is null and (participant_one = (select user_id from public.items where id = item_id) or participant_two = (select user_id from public.items where id = item_id)))
      or
      (workflow_type = 'LOST_FOUND' and lost_found_id is not null and item_id is null and (participant_one = (select user_id from public.lost_found_reports where id = lost_found_id) or participant_two = (select user_id from public.lost_found_reports where id = lost_found_id)))
    )
  );

drop policy if exists "Allow participants to update chats" on public.chats;
create policy "Allow participants to update chats"
  on public.chats for update
  to authenticated
  using (auth.uid() = participant_one or auth.uid() = participant_two);

-- MESSAGES RLS
drop policy if exists "Allow chat participants to read messages" on public.messages;
create policy "Allow chat participants to read messages"
  on public.messages for select
  to authenticated
  using (
    exists (
      select 1 from public.chats c
      where c.id = messages.chat_id
      and (c.participant_one = auth.uid() or c.participant_two = auth.uid())
    )
  );

drop policy if exists "Allow chat participants to insert messages" on public.messages;
create policy "Allow chat participants to insert messages"
  on public.messages for insert
  to authenticated
  with check (
    auth.uid() = sender_id and
    exists (
      select 1 from public.chats c
      where c.id = chat_id
      and (c.participant_one = auth.uid() or c.participant_two = auth.uid())
    )
  );
