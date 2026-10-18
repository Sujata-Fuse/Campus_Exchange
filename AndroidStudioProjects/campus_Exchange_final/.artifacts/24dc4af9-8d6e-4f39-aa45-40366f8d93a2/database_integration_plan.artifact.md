# Campus Exchange - Database Integration Plan

## 1. Inspection Findings & Mock-Data Sources

| Feature / Module | Current Mock Data Source | Required Database Entity |
| :--- | :--- | :--- |
| **Authentication & Profile** | Hardcoded user metadata / `AuthViewModel` / Profile screen static state | `profiles` |
| **Home & Borrow / Lend** | `sampleListings` in `HomeScreen.kt`, static items in `LendItemScreen.kt` & `ItemDetailsAndRequestScreens.kt` | `items` (Lendable Items) |
| **Borrow Requests** | Hardcoded request submission in `RequestToBorrowScreen` | `borrow_requests` |
| **Lost & Found** | `sampleLostFoundItems` in `LostFoundScreens.kt` | `lost_found_reports` |
| **Chat & Messaging** | `sampleConversations` and `sampleMessages` in `ChatScreens.kt` & `ChatModels.kt` | `chats`, `messages` |
| **Activity Feed** | `sampleActivities` in `ActivityScreens.kt` | Derived from `borrow_requests`, `lost_found_reports`, & `messages` |

---

## 2. Proposed PostgreSQL Schema

### Table 1: `profiles`
Stores user profile details tied 1:1 with Supabase Auth users.
```sql
create table public.profiles (
  id uuid references auth.users on delete cascade primary key,
  full_name text not null,
  course text default 'Computer Science • 3rd Year',
  phone text,
  bio text,
  avatar_url text,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### Table 2: `items`
Stores items listed by students for lending or exchange.
```sql
create table public.items (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  price text not null,
  category text not null,
  location text not null,
  image_url text,
  lending_type text default 'Free', -- Free, Fixed Price, Rent
  duration text,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### Table 3: `borrow_requests`
Stores borrow/exchange requests made by students on items.
```sql
create table public.borrow_requests (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete cascade not null,
  requester_id uuid references public.profiles(id) on delete cascade not null,
  owner_id uuid references public.profiles(id) on delete cascade not null,
  status text default 'Pending', -- Pending, Accepted, Rejected, Returned
  message text,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### Table 4: `lost_found_reports`
Stores lost and found item reports.
```sql
create table public.lost_found_reports (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references public.profiles(id) on delete cascade not null,
  title text not null,
  is_lost boolean not null, -- true = Lost, false = Found
  category text not null,
  location text not null,
  description text,
  status text default 'Active', -- Active, Resolved
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### Table 5: `chats`
Stores conversation channels between two users regarding an item or report.
```sql
create table public.chats (
  id uuid default gen_random_uuid() primary key,
  item_id uuid references public.items(id) on delete set null,
  participant_one uuid references public.profiles(id) on delete cascade not null,
  participant_two uuid references public.profiles(id) on delete cascade not null,
  workflow_type text not null, -- BORROW_LEND, LOST_FOUND
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

### Table 6: `messages`
Stores individual chat messages.
```sql
create table public.messages (
  id uuid default gen_random_uuid() primary key,
  chat_id uuid references public.chats(id) on delete cascade not null,
  sender_id uuid references public.profiles(id) on delete cascade not null,
  text text not null,
  created_at timestamp with time zone default timezone('utc'::text, now()) not null
);
```

---

## 3. Relationships, Foreign Keys, & Row Level Security (RLS) Policies

- **Foreign Keys**: Ensure referential integrity across users (`auth.users`), `profiles`, `items`, `borrow_requests`, `lost_found_reports`, `chats`, and `messages`. `ON DELETE CASCADE` is used where child entities depend entirely on parents.
- **Row Level Security (RLS)**:
  - **`profiles`**: Public read for authenticated users; users can update only their own profile (`id = auth.uid()`).
  - **`items`**: Public read for authenticated users; insert, update, delete restricted to item owner (`user_id = auth.uid()`).
  - **`borrow_requests`**: Readable and writable by the requester (`requester_id = auth.uid()`) or item owner (`owner_id = auth.uid()`).
  - **`lost_found_reports`**: Public read; insert, update, delete restricted to reporter (`user_id = auth.uid()`).
  - **`chats` & `messages`**: Accessible only by participants (`participant_one = auth.uid() or participant_two = auth.uid()`).

---

## 4. User Full Name Synchronization Strategy

1. **Storage during Registration**: When a student signs up via `AuthViewModel.signUp`, `full_name` is stored in Supabase Auth user metadata (`user_metadata["full_name"]`).
2. **Database Trigger**: A Postgres trigger automatically inserts a corresponding row into `public.profiles` upon user creation:
   ```sql
   create function public.handle_new_user()
   returns trigger as $$
   begin
     insert into public.profiles (id, full_name)
     values (new.id, new.raw_user_meta_data->>'full_name');
     return new;
   end;
   $$ language plpgsql security definer;

   create trigger on_auth_user_created
     after insert on auth.users
     for each row execute procedure public.handle_new_user();
   ```
3. **Retrieval**: The app reads `full_name` from Supabase Auth user metadata instantly on login/home screen, and fetches detailed profile records from the `profiles` table when visiting the Profile screen.

---

## 5. Recommended Implementation Order

1. **Phase 1 (`profiles`)**: Set up table, RLS policies, and Auth trigger for profile creation.
2. **Phase 2 (`items`)**: Integrate Lend Item and Home Screen item listings with Supabase Postgrest.
3. **Phase 3 (`borrow_requests`)**: Connect item borrow/exchange requests.
4. **Phase 4 (`lost_found_reports`)**: Connect lost & found reporting dashboard.
5. **Phase 5 (`chats` & `messages`)**: Implement real-time chat messaging using Supabase Realtime/Postgrest.
