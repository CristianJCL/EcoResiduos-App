-- Ejecutar en el SQL Editor del proyecto Supabase propio.
-- Usa Auth (correo y contraseña). La clave service_role NUNCA va en Android.
create table if not exists public.residuos (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  type text not null check (type in ('FOOD', 'FRUIT_VEGETABLE', 'GARDEN')),
  weight_kg double precision not null check (weight_kg > 0),
  source text not null,
  date text not null,
  time text not null,
  composted boolean not null default false,
  notes text not null default ''
);
create index if not exists idx_residuos_owner on public.residuos(user_id);
alter table public.residuos enable row level security;
drop policy if exists "residuos_propios" on public.residuos;
create policy "residuos_propios" on public.residuos
  for all to authenticated using (auth.uid() = user_id)
  with check (auth.uid() = user_id);
grant select, insert, update, delete on public.residuos to authenticated;
