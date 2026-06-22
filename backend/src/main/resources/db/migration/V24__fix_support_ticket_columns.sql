-- Fix: add missing updated_at column and widen status for longer values
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE support_ticket ALTER COLUMN status TYPE VARCHAR(30);
