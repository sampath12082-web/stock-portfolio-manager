CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,

    symbol VARCHAR(20) NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    exchange VARCHAR(20) NOT NULL,

    sector VARCHAR(100),
    industry VARCHAR(100),

    created_at TIMESTAMP,
    updated_at TIMESTAMP
);