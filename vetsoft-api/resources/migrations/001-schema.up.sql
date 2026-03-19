CREATE TABLE license_tiers (
  id          SERIAL PRIMARY KEY,
  name        TEXT NOT NULL,
  version     TEXT NOT NULL,
  price       INTEGER NOT NULL,
  badge_text  TEXT,
  badge_color TEXT,
  highlight   BOOLEAN DEFAULT FALSE,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ DEFAULT NOW()
);

--;;

CREATE TABLE extra_items (
  id          SERIAL PRIMARY KEY,
  category    TEXT NOT NULL CHECK (category IN ('workstation', 'upgrade')),
  name        TEXT NOT NULL,
  description TEXT,
  price       INTEGER NOT NULL,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ DEFAULT NOW()
);

--;;

CREATE TABLE modules (
  id          SERIAL PRIMARY KEY,
  name        TEXT NOT NULL,
  note        TEXT,
  price       INTEGER NOT NULL,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ DEFAULT NOW()
);

--;;

CREATE TABLE support_rates (
  id            SERIAL PRIMARY KEY,
  license_range TEXT NOT NULL,
  price         INTEGER NOT NULL,
  sort_order    INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMPTZ DEFAULT NOW(),
  updated_at    TIMESTAMPTZ DEFAULT NOW()
);

--;;

CREATE TABLE services (
  id          SERIAL PRIMARY KEY,
  name        TEXT NOT NULL,
  price       TEXT NOT NULL,
  full_width  BOOLEAN DEFAULT FALSE,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ DEFAULT NOW()
);

--;;

CREATE TABLE price_metadata (
  id    SERIAL PRIMARY KEY,
  key   TEXT UNIQUE NOT NULL,
  value TEXT NOT NULL
);
