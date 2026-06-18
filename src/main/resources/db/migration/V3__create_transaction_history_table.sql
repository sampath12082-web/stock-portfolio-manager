CREATE TABLE transaction_history (
    id BIGSERIAL PRIMARY KEY,

    stock_id BIGINT NOT NULL,

    transaction_type VARCHAR(20) NOT NULL,

    quantity NUMERIC(20,4) NOT NULL,

    price NUMERIC(20,4) NOT NULL,

    transaction_date DATE NOT NULL,

    created_at TIMESTAMP,

    CONSTRAINT fk_transaction_stock
    FOREIGN KEY(stock_id)
    REFERENCES stock(id)
);