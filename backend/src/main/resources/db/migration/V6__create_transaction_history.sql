CREATE TABLE transaction_history (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    exchange VARCHAR(10) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_transaction_stock
        FOREIGN KEY (stock_id)
        REFERENCES stock(id)
);