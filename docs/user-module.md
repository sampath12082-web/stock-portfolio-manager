# User Module Documentation

## Overview

JWT-based authentication system with email registration, OTP verification, security questions, role-based access control, admin user management, and profile editing. All sensitive data (passwords, security answers) are encrypted in transit using RSA public-key encryption — passwords never appear in plain text in browser dev tools.

---

## User Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Own portfolio data, profile management |
| `ROLE_ADMIN` | All user data + user management (list, reset password, activate/deactivate, delete) |

## Admin User

- **Email**: `sampath12082@gmail.com`
- **Default Password**: `Admin@123456789!` (configurable via `ADMIN_DEFAULT_PASSWORD` env var)
- Seeded automatically on first startup by `AdminUserSeeder`
- **Email verified**: YES (pre-verified on seed)
- All existing portfolio data (stocks, holdings, transactions, MF) assigned to this user
- Cannot be deleted via admin panel

---

## Password Policy

| Rule | Requirement |
|------|-------------|
| Length | 16–20 characters |
| Uppercase | At least 1 uppercase letter (A-Z) |
| Lowercase | At least 1 lowercase letter (a-z) |
| Numeric | At least 1 digit (0-9) |
| Special | At least 1 special character (!@#$%^&*) |

**Regex**: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{16,20}$`

Validated on:
- Registration
- Password reset
- Password change

---

## Security Questions

Each user must set up **2 security questions** during registration. These are used to verify identity before sending a password reset OTP.

### Available Questions

1. What is your mother's maiden name?
2. What was the name of your first pet?
3. What city were you born in?
4. What is the name of your first school?
5. What is your favorite movie?
6. What was your childhood nickname?
7. What is the name of the street you grew up on?
8. What is your favorite book?

### Storage
- Security question answers are stored as **BCrypt hashes** (not plain text)
- User selects 2 different questions and provides answers during registration
- Answers are case-insensitive (lowercased before hashing)

---

## Network Security — Sensitive Data Encryption

### Problem
Passwords and security answers should not appear in plain text in browser dev tools (Network tab).

### Solution: RSA Public-Key Encryption

```
Frontend                              Backend
────────                              ───────
1. GET /api/auth/public-key     →     Returns RSA public key (2048-bit)
2. Encrypt password with          
   public key (RSA-OAEP)
3. POST /api/auth/login           →   Decrypt with RSA private key
   { email, encryptedPassword }       Validate credentials
```

**How it works:**
- Backend generates RSA 2048-bit key pair on startup
- Frontend fetches the public key before any auth operation
- Frontend encrypts sensitive fields (password, security answers) using `window.crypto.subtle` (Web Crypto API) with RSA-OAEP
- Backend decrypts with the private key before processing
- Passwords never travel in plain text — dev tools shows encrypted Base64 string

**Encrypted fields:**
- `password` in login, register, change-password, reset-password
- `securityAnswer1`, `securityAnswer2` in register and forgot-password

**NOT encrypted** (not sensitive):
- `email`, `firstName`, `lastName`, `otpCode`

---

## Authentication Flow

### Registration
```
1. User submits: email, encryptedPassword, firstName, lastName,
   securityQuestion1, encryptedAnswer1, securityQuestion2, encryptedAnswer2
2. Backend decrypts password and answers with RSA private key
3. Validates password policy (16-20 chars, upper+lower+digit+special)
4. Validates email is unique
5. Validates 2 different security questions selected
6. Creates user (email_verified=false)
7. Hashes password with BCrypt, hashes answers with BCrypt
8. Sends 6-digit OTP to email (10-min expiry)
9. User enters OTP → /api/auth/verify-otp
10. Backend sets email_verified=true
11. User can now login
```

**Email verification is MANDATORY** — user cannot login until email is verified.

### Login
```
1. User submits: email, encryptedPassword
2. Backend decrypts password with RSA private key
3. Validates credentials (BCrypt match)
4. Checks email_verified=true (rejects if false)
5. Checks status=ACTIVE (rejects INACTIVE/SUSPENDED)
6. Returns JWT access token (15min) + refresh token (7 days)
```

### Forgot Password (3-step flow)
```
Step 1: Security Questions
  → User enters email → /api/auth/forgot-password
  → Backend returns the 2 security questions for this user (not answers)
  → User answers both questions (encrypted)
  → Backend decrypts and validates answers (BCrypt match)
  → If valid: sends OTP to email
  → If invalid: returns error "Security answers do not match"

Step 2: OTP Verification
  → User enters 6-digit OTP → /api/auth/verify-reset-otp
  → Backend validates OTP (not expired, not used)
  → Returns a one-time reset token

Step 3: Set New Password
  → User enters new password (encrypted) → /api/auth/reset-password
  → Backend decrypts, validates password policy
  → Updates password hash
```

### Change Password
```
Authenticated user → /api/auth/change-password
  → Sends: encryptedCurrentPassword, encryptedNewPassword
  → Backend decrypts both
  → Validates current password matches
  → Validates new password policy
  → Updates hash
```

### Logout
```
Frontend clears localStorage (accessToken, refreshToken, user)
  → Redirects to /login
  → No server-side session to invalidate (stateless JWT)
```

---

## API Endpoints

### Public (no auth required)

| Method | Endpoint | Request Body | Response |
|--------|----------|-------------|----------|
| GET | `/api/auth/public-key` | — | `{publicKey}` (RSA PEM) |
| POST | `/api/auth/register` | `{email, encryptedPassword, firstName, lastName?, securityQuestion1, encryptedAnswer1, securityQuestion2, encryptedAnswer2}` | `UserResponse` (201) |
| POST | `/api/auth/verify-otp` | `{email, otpCode}` | `{message}` |
| POST | `/api/auth/login` | `{email, encryptedPassword}` | `{accessToken, refreshToken, user}` |
| POST | `/api/auth/forgot-password` | `{email}` | `{securityQuestion1, securityQuestion2}` |
| POST | `/api/auth/verify-security` | `{email, encryptedAnswer1, encryptedAnswer2}` | `{message}` (sends OTP) |
| POST | `/api/auth/verify-reset-otp` | `{email, otpCode}` | `{resetToken}` |
| POST | `/api/auth/reset-password` | `{email, resetToken, encryptedNewPassword}` | `{message}` |
| POST | `/api/auth/refresh` | `{refreshToken}` | `{accessToken, refreshToken, user}` |

### Authenticated

| Method | Endpoint | Request Body | Response |
|--------|----------|-------------|----------|
| POST | `/api/auth/change-password` | `{encryptedCurrentPassword, encryptedNewPassword}` | `{message}` |
| GET | `/api/profile` | — | `UserResponse` |
| PUT | `/api/profile` | `{firstName?, lastName?, phone?}` | `UserResponse` |

### Admin Only (ROLE_ADMIN)

| Method | Endpoint | Request Body | Response |
|--------|----------|-------------|----------|
| GET | `/api/admin/users` | — | `UserResponse[]` |
| GET | `/api/admin/users/{id}` | — | `UserResponse` |
| PUT | `/api/admin/users/{id}/status` | `{status: "ACTIVE"/"INACTIVE"/"SUSPENDED"}` | `UserResponse` |
| POST | `/api/admin/users/{id}/reset-password` | — | `{message, temporaryPassword}` |
| DELETE | `/api/admin/users/{id}` | — | `{message}` |

---

## Data Model

### Users Table (V15)

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | — |
| phone | VARCHAR(20) | — |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'ROLE_USER' |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' |
| email_verified | BOOLEAN | NOT NULL, DEFAULT FALSE |
| security_question_1 | VARCHAR(255) | — |
| security_answer_1_hash | VARCHAR(255) | — |
| security_question_2 | VARCHAR(255) | — |
| security_answer_2_hash | VARCHAR(255) | — |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Status values**: `ACTIVE`, `INACTIVE`, `SUSPENDED`

### OTP Tokens Table (V16)

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| email | VARCHAR(255) | NOT NULL |
| otp_code | VARCHAR(6) | NOT NULL |
| purpose | VARCHAR(20) | NOT NULL |
| expires_at | TIMESTAMP | NOT NULL |
| used | BOOLEAN | DEFAULT FALSE |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**Purpose values**: `REGISTRATION`, `PASSWORD_RESET`

### Multi-Tenant FK (V17)

All domain tables have `user_id BIGINT REFERENCES users(id)`:
- stock, holding, transaction_history, portfolio_snapshot
- trading_signal, mutual_fund, mf_holding, mf_transaction

---

## Frontend Pages

### Login (`/login`)
- Email + encrypted password form
- Error messages for invalid credentials, unverified email, inactive account
- Links to Register and Forgot Password
- Fetches RSA public key on mount, encrypts password before sending
- Redirects to Dashboard on success

### Register (`/register`)
- Step 1: Email, first name, last name, password (with policy indicator)
- Step 2: Select 2 security questions from dropdown, provide answers
- Step 3: OTP verification (6-digit code sent to email)
- Password policy shown inline (checkmarks for each rule met)
- All sensitive fields encrypted with RSA before sending

### Forgot Password (`/forgot-password`)
- Step 1: Enter email → backend returns security questions
- Step 2: Answer both security questions → backend validates, sends OTP
- Step 3: Enter OTP → backend validates
- Step 4: Enter new password (with policy check) → backend resets
- Redirects to Login on success

### Profile (`/profile`)
- **Personal Details** card: email (read-only), first name, last name, phone, role
- **Change Password** card: current password + new password (both encrypted)
- Save button updates profile via PUT /api/profile

### Header
- Shows user's first name with User icon
- Profile link navigates to /profile
- Red Logout button clears tokens and redirects to /login

---

## Security Architecture

### Network Encryption (RSA)
```
Startup:
  Backend generates RSA 2048-bit key pair (in-memory, regenerated on restart)

Auth Request:
  Frontend: GET /api/auth/public-key → receives PEM public key
  Frontend: window.crypto.subtle.encrypt(RSA-OAEP, publicKey, password)
  Frontend: POST /api/auth/login { email, encryptedPassword: "<base64>" }
  
  Dev Tools shows: { email: "user@example.com", encryptedPassword: "Xk9fR2..." }
  (password NOT visible in plain text)

  Backend: RSA decrypt with private key → plain password
  Backend: BCrypt verify against stored hash
```

### JWT Token Security
```
Access Token (15min):
  - Contains: email, role
  - Signed with HMAC-SHA384
  - Sent via Authorization: Bearer header

Refresh Token (7 days):
  - Contains: email only
  - Used to get new access token
  - Stored in localStorage
```

### Endpoint Protection
```
/api/auth/**     → permitAll (public)
/api/admin/**    → hasAuthority('ROLE_ADMIN')
/api/**          → authenticated
/**              → permitAll (static files, SPA routes)
```

---

## Implementation Files

### Backend

| File | Purpose |
|------|---------|
| `entity/User.java` | User entity (with security question fields) |
| `entity/OtpToken.java` | OTP token entity |
| `repository/UserRepository.java` | findByEmail, existsByEmail |
| `repository/OtpTokenRepository.java` | Find latest unused OTP |
| `service/AuthService.java` | Auth interface |
| `service/impl/AuthServiceImpl.java` | Register, login, OTP, security questions, password management |
| `service/JwtService.java` | JWT interface |
| `service/impl/JwtServiceImpl.java` | Token generation/validation (jjwt 0.12.6) |
| `service/EmailService.java` | Email interface |
| `service/impl/EmailServiceImpl.java` | OTP email via Spring Mail |
| `service/RsaKeyService.java` | RSA key pair management |
| `controller/AuthController.java` | /api/auth/* endpoints (including public-key) |
| `controller/AdminController.java` | /api/admin/* endpoints |
| `controller/ProfileController.java` | /api/profile endpoints |
| `security/JwtAuthenticationFilter.java` | OncePerRequestFilter — JWT validation |
| `security/JwtAuthEntryPoint.java` | 401 response handler |
| `security/UserDetailsServiceImpl.java` | Load user from DB for Spring Security |
| `config/SecurityConfig.java` | JWT filter chain, BCrypt, endpoint rules |
| `config/AdminUserSeeder.java` | Seeds admin user on first startup |

### Frontend

| File | Purpose |
|------|---------|
| `auth/AuthContext.tsx` | React context — token state, login/logout |
| `auth/AuthGuard.tsx` | Route guards (AuthGuard, AdminGuard) |
| `auth/types.ts` | UserResponse, AuthResponse types |
| `auth/crypto.ts` | RSA encryption utilities (fetchPublicKey, encryptField) |
| `api/client.ts` | Axios interceptors (request: add token, response: 401 → /login) |
| `pages/LoginPage.tsx` | Login form with RSA-encrypted password |
| `pages/RegisterPage.tsx` | Register + security questions + OTP |
| `pages/ForgotPasswordPage.tsx` | Security questions → OTP → reset |
| `pages/ProfilePage.tsx` | Profile edit + change password |

---

## Database Migrations

| Version | Purpose |
|---------|---------|
| V15 | Create `users` table (with security question columns) |
| V16 | Create `otp_tokens` table |
| V17 | Add `user_id` FK to all domain tables |
| V18 | Assign existing data to admin user (user_id=1) |
| V19 | Add security question columns to users table (if not in V15) |

---

## Environment Variables

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `JWT_SECRET` | Yes | dev fallback | Base64-encoded HMAC signing key |
| `JWT_EXPIRATION` | No | `900000` (15min) | Access token TTL in ms |
| `JWT_REFRESH_EXPIRATION` | No | `604800000` (7d) | Refresh token TTL in ms |
| `ADMIN_DEFAULT_PASSWORD` | No | `Admin@123456789!` | Initial admin password (must meet policy) |
| `SPRING_MAIL_HOST` | For OTP | `smtp.gmail.com` | SMTP server |
| `SPRING_MAIL_PORT` | For OTP | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | For OTP | — | SMTP username |
| `SPRING_MAIL_PASSWORD` | For OTP | — | SMTP app password |

---

## Enhancements Status

| # | Description | Status |
|---|-------------|--------|
| 22 | User registration with email + password + name | Backend done, frontend done |
| 23 | JWT login (access + refresh tokens) | Done |
| 24 | Email OTP verification (mandatory for new users) | Backend done, frontend done |
| 25 | Forgot password with security questions + OTP | Documentation ready, implementation pending |
| 26 | Change password (16-20 chars, complexity rules) | Documentation ready, implementation pending |
| 27 | Spring Security JWT filter | Done |
| 28 | Profile page (email immutable) | Done |
| 29 | Admin user seeded (email verified) | Done |
| 30 | Admin panel (user management) | Backend done, frontend pending |
| 34 | Password policy (16-20 chars, upper+lower+digit+special) | Documentation ready, implementation pending |
| 35 | Security questions (2 per user, BCrypt hashed) | Documentation ready, implementation pending |
| 36 | RSA encryption for passwords in transit | Documentation ready, implementation pending |
