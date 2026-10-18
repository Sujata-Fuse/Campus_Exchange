# Campus Exchange - Complete App Analysis & Database Schema Plan

## 1. Application Data Requirements & Workflows

Based on deep inspection of the entire Campus Exchange codebase across all screens, navigation destinations, and ViewModels:

1. **Authentication & Student Profiles**:
   - **Data Required**: Student ID, full name, course & year, phone number (optional), bio/about, avatar URL, creation timestamp.
   - **Source**: Integrated with Supabase Auth (`user_metadata["full_name"]`) & Postgrest (`public.profiles`).

2. **Home Dashboard & Marketplace (Lend / Borrow)**:
   - **Data Required**: Item ID, seller/lender user ID, title, category (Books, Electronics, Fashion, Furniture, Notes, Sports, Other), price, location, image URL, lending type (Free, Fixed Price, Rent on Duration), duration, creation timestamp.
   - **Current Mock Source**: `sampleListings` in `HomeScreen.kt`.
   - **Target Table**: `public.items`.

3. **Borrow Requests**:
   - **Data Required**: Request ID, item ID, requester ID, owner ID, status (Pending, Accepted, Rejected, Returned), optional message, requested duration, timestamp.
   - **Current Mock Source**: Local Compose state in `RequestToBorrowScreen`.
   - **Target Table**: `public.borrow_requests`.

4. **Lost & Found Dashboard**:
   - **Data Required**: Report ID, reporter user ID, title, is_lost flag (`true` for Lost, `false` for Found), category, location, description, status (Active, Resolved), creation timestamp.
   - **Current Mock Source**: `sampleLostFoundItems` in `LostFoundScreens.kt`.
   - **Target Table**: `public.lost_found_reports`.

5. **Chat & Messaging**:
   - **Data Required**:
     - **Chats**: Conversation ID, item ID (optional), participant_one user ID, participant_two user ID, workflow_type (`BORROW_LEND`, `LOST_FOUND`), last_message text, timestamp.
     - **Messages**: Message ID, chat ID, sender user ID, text, timestamp.
   - **Current Mock Source**: `sampleConversations` and `sampleMessages` in `ChatScreens.kt`.
   - **Target Tables**: `public.chats`, `public.messages`.

6. **Activity Feed**:
   - **Data Required**: Notifications regarding borrow request status changes, new matches, and message alerts.
   - **Current Mock Source**: `sampleActivities` in `ActivityScreens.kt`.
   - **Target Tables**: Derived dynamically from `borrow_requests`, `lost_found_reports`, and `messages`.

---

## 2. Complete PostgreSQL Database Migration (`supabase_schema.sql`)

```sql
-- =========================================================
-- CAMPUS EXCHANGE - COMPLETE POSTGRESQL DATABASE MIGRATION
-- Copy & Run this script in Supabase Dashboard -> SQL Editor
-- =========================================================

-- 1. PROFILES TABLE
create table if not exists public.profiles (
  id uuid references auth.users on delete cascade primary key,
  full_name text not null,
  course text default 'Computer Science • 3rd Year',
  phone text default '',
  bio text default '',
  avatar_url text default '',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 2. ITEMS (LENDABLE ITEMS / MARKETPLACE) TABLE
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

-- 3. BORROW REQUESTS TABLE
create table if not exists public.borrow_requests (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete cascade not null,
  requester_id uuid references public.profiles(id) on delete cascade not null,
  owner_id uuid references public.profiles(id) on delete cascade not null,
  status text default 'Pending', -- Pending, Accepted, Rejected, Returned
  message text default '',
  duration text default '7 Days',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 4. LOST & FOUND REPORTS TABLE
create table if not exists public.lost_found_reports (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  is_lost boolean not null, -- true = Lost, false = Found
  category text not null,
  location text not null,
  description text default '',
  status text default 'Active', -- Active, Resolved
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 5. CHATS (CONVERSATIONS) TABLE
create table if not exists public.chats (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete set null,
  participant_one uuid references public.profiles(id) on delete cascade not null,
  participant_two uuid references public.profiles(id) on delete cascade not null,
  workflow_type text default 'BORROW_LEND', -- BORROW_LEND, LOST_FOUND
  last_message text default '',
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 6. MESSAGES TABLE
create table if not exists public.messages (
  id uuid default gen_random_uuid() primary key,
  chat_id uuid references public.chats(id) on delete cascade not null,
  sender_id uuid references public.profiles(id) on delete cascade not null,
  text text not null,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- =========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- =========================================================

alter table public.profiles enable row level security;
alter table public.items enable row level security;
alter table public.borrow_requests enable row level security;
alter table public.lost_found_reports enable row level security;
alter table public.chats enable row level security;
alter table public.messages enable row level security;

-- PROFILES
drop policy if exists "Allow public read access to profiles" on public.profiles;
create policy "Allow public read access to profiles" on public.profiles for select to authenticated using (true);

drop policy if exists "Allow users to insert their own profile" on public.profiles;
create policy "Allow users to insert their own profile" on public.profiles for insert to authenticated with check (auth.uid() = id);

drop policy if exists "Allow users to update their own profile" on public.profiles;
create policy "Allow users to update their own profile" on public.profiles for update to authenticated using (auth.uid() = id) with check (auth.uid() = id);

-- ITEMS
drop policy if exists "Allow public read access to items" on public.items;
create policy "Allow public read access to items" on public.items for select to authenticated using (true);

drop policy if exists "Allow authenticated users to insert items" on public.items;
create policy "Allow authenticated users to insert items" on public.items for insert to authenticated with check (auth.uid() = user_id);

drop policy if exists "Allow owners to update their items" on public.items;
create policy "Allow owners to update their items" on public.items for update to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "Allow owners to delete their items" on public.items;
create policy "Allow owners to delete their items" on public.items for delete to authenticated using (auth.uid() = user_id);

-- BORROW REQUESTS
drop policy if exists "Allow participants to read borrow requests" on public.borrow_requests;
create policy "Allow participants to read borrow requests" on public.borrow_requests for select to authenticated using (auth.uid() = requester_id or auth.uid() = owner_id);

drop policy if exists "Allow requesters to insert borrow requests" on public.borrow_requests;
create policy "Allow requesters to insert borrow requests" on public.borrow_requests for insert to authenticated with check (auth.uid() = requester_id);

drop policy if exists "Allow participants to update borrow requests" on public.borrow_requests;
create policy "Allow participants to update borrow requests" on public.borrow_requests for update to authenticated using (auth.uid() = requester_id or auth.uid() = owner_id);

-- LOST & FOUND
drop policy if exists "Allow public read access to lost found reports" on public.lost_found_reports;
create policy "Allow public read access to lost found reports" on public.lost_found_reports for select to authenticated using (true);

drop policy if exists "Allow authenticated users to insert lost found reports" on public.lost_found_reports;
create policy "Allow authenticated users to insert lost found reports" on public.lost_found_reports for insert to authenticated with check (auth.uid() = user_id);

drop policy if exists "Allow reporters to update lost found reports" on public.lost_found_reports;
create policy "Allow reporters to update lost found reports" on public.lost_found_reports for update to authenticated using (auth.uid() = user_id);

-- CHATS
drop policy if exists "Allow participants to read chats" on public.chats;
create policy "Allow participants to read chats" on public.chats for select to authenticated using (auth.uid() = participant_one or auth.uid() = participant_two);

drop policy if exists "Allow participants to insert chats" on public.chats;
create policy "Allow participants to insert chats" on public.chats for insert to authenticated with check (auth.uid() = participant_one or auth.uid() = participant_two);

-- MESSAGES
drop policy if exists "Allow chat participants to read messages" on public.messages;
create policy "Allow chat participants to read messages" on public.messages for select to authenticated using (
  exists (
    select 1 from public.chats c
    where c.id = messages.chat_id
    and (c.participant_one = auth.uid() or c.participant_two = auth.uid())
  )
);

drop policy if exists "Allow chat participants to insert messages" on public.messages;
create policy "Allow chat participants to insert messages" on public.messages for insert to authenticated with check (
  auth.uid() = sender_id and
  exists (
    select 1 from public.chats c
    where c.id = messages.chat_id
    and (c.participant_one = auth.uid() or c.participant_two = auth.uid())
  )
);

-- =========================================================
-- AUTOMATIC PROFILE TRIGGER
-- =========================================================
create or replace function public.handle_new_user()
returns trigger as $$
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
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
```
