CREATE TABLE faq (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    sort_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO faq (category, question, answer, sort_order) VALUES
('Getting Started', 'How do I add stocks to my portfolio?', 'Go to the Stocks page, click "Add Stock", search by name or symbol, and click + to add. You can also sync from Groww using the "Sync from Groww" button on the Holdings page.', 1),
('Getting Started', 'How do I sync my Groww portfolio?', 'Navigate to Holdings page and click "Sync from Groww". This requires a valid Groww API key configured in your settings. The API key must be renewed daily at groww.in/trade-api.', 2),
('Getting Started', 'How do I track mutual funds?', 'Go to the Mutual Funds page, click "Add Fund", search by scheme name from the AMFI database, and add your holdings with units and average NAV.', 3),
('Portfolio', 'How is Realized P&L calculated?', 'Realized P&L = (Total Invested in Current Holdings + Clear Cash) - Total Deposited. This uses Groww clear cash as the source of truth.', 4),
('Portfolio', 'How is Unrealized P&L calculated?', 'Unrealized P&L = Current Market Value - Invested Amount. This is computed using live prices from Yahoo Finance.', 5),
('Portfolio', 'What is the difference between CNC and MIS trades?', 'CNC (Cash and Carry) are delivery trades where you hold stocks overnight. MIS (Margin Intraday Settlement) are intraday trades bought and sold the same day.', 6),
('Portfolio', 'Why does the dashboard show "Groww offline"?', 'The Groww API key expires daily. Renew it at groww.in/trade-api/api-keys. Without a valid key, cash balance and realized P&L cannot be computed.', 7),
('Trading Signals', 'How are trading signals generated?', 'Signals are generated using technical analysis: SMA crossover (20/50 day), RSI (14 day), 52-week position, and volume trends. They run automatically at 4 PM on weekdays.', 8),
('Trading Signals', 'What do the signal types mean?', 'BUY_SIGNAL: stock looks undervalued. SELL_SIGNAL: stock looks overvalued. HOLD: no clear direction. WATCH: worth monitoring.', 9),
('Account', 'How do I change my password?', 'Go to your Profile page (click your name in the header), then use the Change Password section. Password must be 16-20 characters with uppercase, lowercase, digit, and special character.', 10),
('Account', 'Can I change my email address?', 'No. Email is your unique login identifier and cannot be changed after registration.', 11),
('Account', 'How do I contact support?', 'Use the "Submit Request" form on the Help page. Your request will be reviewed by an administrator.', 12);
