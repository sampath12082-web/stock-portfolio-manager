ALTER TABLE stock ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE holding ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE transaction_history ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE portfolio_snapshot ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE trading_signal ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE mutual_fund ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE mf_holding ADD COLUMN user_id BIGINT REFERENCES users(id);
ALTER TABLE mf_transaction ADD COLUMN user_id BIGINT REFERENCES users(id);
