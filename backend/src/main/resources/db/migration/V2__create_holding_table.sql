CREATE TABLE holding (
    id BIGSERIAL PRIMARY KEY,

    stock_id BIGINT NOT NULL,

    quantity Integer NOT NULL,

    average_buy_price NUMERIC(20,4) NOT NULL,

    invested_amount NUMERIC(20,2) NOT NULL,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_holding_stock
    FOREIGN KEY(stock_id)
    REFERENCES stock(id)
);