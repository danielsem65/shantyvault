# ShantyVault Web

Web client for ShantyVault — a secure file vault and notes app powered by Supabase.

## Prerequisites

- **Node.js 18+** — [Download](https://nodejs.org/)
- **Supabase project** — already set up at `https://iprjbhkxbqnheawligte.supabase.co`

## Setup

### 1. Create Supabase tables

Open your [Supabase SQL Editor](https://supabase.com/dashboard/project/iprjbhkxbqnheawligte/sql/new), paste the contents of `schema.sql`, and run it.

This creates the `notes`, `files`, and `activities` tables with Row Level Security.

### 2. Configure Redirect URLs

In [Supabase Auth Settings](https://supabase.com/dashboard/project/iprjbhkxbqnheawligte/auth/settings):

- **Site URL**: `http://localhost:3000`
- **Redirect URLs**: add `http://localhost:3000` (for password reset flow)

### 3. Install & run

```bash
cd web
npm install
npm run dev
```

Opens at **http://localhost:3000**

## Features

- **Auth** — register, login, password reset via Supabase Auth
- **Dashboard** — storage stats, recent notes overview
- **Files** — upload/download/delete files in Supabase Storage (`vault-files` bucket)
- **Notes** — create, edit, pin, search, delete notes (stored in Supabase DB)

## Deploy to Render

1. Push this repo to GitHub
2. In [Render Dashboard](https://dashboard.render.com), create a **New Static Site**
3. Connect your repo, set:
   - **Build Command**: `cd web && npm install && npm run build`
   - **Publish Directory**: `web/dist`
4. Add environment variables:
   - `VITE_SUPABASE_URL` = `https://iprjbhkxbqnheawligte.supabase.co`
   - `VITE_SUPABASE_ANON_KEY` = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
5. Deploy

Then update the Supabase Auth **Site URL** and **Redirect URLs** to your Render URL.
