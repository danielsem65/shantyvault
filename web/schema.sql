-- Run this in your Supabase SQL Editor (https://supabase.com/dashboard/project/iprjbhkxbqnheawligte/sql/new)

-- Enable Row Level Security
alter table if exists public.notes disable row level security;
alter table if exists public.files disable row level security;

-- Notes table
create table if not exists public.notes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  title text not null default '',
  content text not null default '',
  is_pinned boolean not null default false,
  color_hex text,
  has_checklist boolean not null default false,
  checklist_data text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Files metadata table
create table if not exists public.files (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  extension text not null default '',
  mime_type text not null default '',
  size bigint not null default 0,
  folder_id text,
  remote_path text not null,
  is_favorite boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- Activities table
create table if not exists public.activities (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  type text not null,
  description text not null default '',
  item_id text,
  item_name text,
  metadata jsonb,
  created_at timestamptz not null default now()
);

-- Indexes
create index if not exists notes_user_id_idx on public.notes(user_id);
create index if not exists notes_updated_at_idx on public.notes(updated_at desc);
create index if not exists files_user_id_idx on public.files(user_id);
create index if not exists files_folder_id_idx on public.files(folder_id);
create index if not exists activities_user_id_idx on public.activities(user_id);
create index if not exists activities_created_at_idx on public.activities(created_at desc);

-- Row Level Security
alter table public.notes enable row level security;
alter table public.files enable row level security;
alter table public.activities enable row level security;

-- Policies: users can only see their own data
create policy "Users can manage their own notes"
  on public.notes for all using (auth.uid() = user_id);

create policy "Users can manage their own files"
  on public.files for all using (auth.uid() = user_id);

create policy "Users can manage their own activities"
  on public.activities for all using (auth.uid() = user_id);
