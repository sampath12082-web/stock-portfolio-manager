CREATE TABLE stock_quote (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    last_traded_price NUMERIC(19,2) NOT NULL,
    open_price NUMERIC(19,2),
    high_price NUMERIC(19,2),
    low_price NUMERIC(19,2),
    close_price NUMERIC(19,2),
    previous_close NUMERIC(19,2),
    volume BIGINT,
    fetched_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_quote_stock FOREIGN KEY (stock_id) REFERENCES stock(id)
);

CREATE INDEX idx_stock_quote_stock_id ON stock_quote(stock_id);
CREATE INDEX idx_stock_quote_fetched_at ON stock_quote(fetched_at DESC);
