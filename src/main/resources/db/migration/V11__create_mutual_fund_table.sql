CREATE TABLE mutual_fund (
    id BIGSERIAL PRIMARY KEY,
    scheme_code VARCHAR(20) NOT NULL UNIQUE,
    scheme_name VARCHAR(500) NOT NULL,
    fund_house VARCHAR(255),
    isin VARCHAR(20),
    category VARCHAR(100),
    fund_type VARCHAR(100),
    current_nav NUMERIC(19,4),
    nav_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_mf_scheme_code ON mutual_fund(scheme_code);
CREATE INDEX idx_mf_fund_house ON mutual_fund(fund_house);
