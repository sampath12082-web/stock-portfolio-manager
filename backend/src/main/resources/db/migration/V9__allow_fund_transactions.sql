ALTER TABLE transaction_history ALTER COLUMN stock_id DROP NOT NULL;
ALTER TABLE transaction_history ALTER COLUMN exchange DROP NOT NULL;
ALTER TABLE transaction_history ADD COLUMN description VARCHAR(255);
