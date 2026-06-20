ALTER TABLE trading_signal ADD COLUMN source VARCHAR(20) DEFAULT 'MANUAL';
CREATE INDEX idx_signal_source ON trading_signal(source);
