-- Admin user will be created by AdminUserSeeder (ApplicationRunner) on first startup
-- This migration assigns all existing data to user_id=1 (admin) after the seeder runs
-- If no user with id=1 exists yet, these updates are safe (FK allows null from V17)

UPDATE stock SET user_id = 1 WHERE user_id IS NULL;
UPDATE holding SET user_id = 1 WHERE user_id IS NULL;
UPDATE transaction_history SET user_id = 1 WHERE user_id IS NULL;
UPDATE portfolio_snapshot SET user_id = 1 WHERE user_id IS NULL;
UPDATE trading_signal SET user_id = 1 WHERE user_id IS NULL;
UPDATE mutual_fund SET user_id = 1 WHERE user_id IS NULL;
UPDATE mf_holding SET user_id = 1 WHERE user_id IS NULL;
UPDATE mf_transaction SET user_id = 1 WHERE user_id IS NULL;
