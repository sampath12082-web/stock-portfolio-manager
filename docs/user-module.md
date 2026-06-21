# User Module Documentation

## Overview

JWT-based authentication system with email registration, OTP verification, role-based access control, admin user management, and profile editing.

---

## User Roles

| Role | Access |
|------|--------|
| `ROLE_USER` | Own portfolio data, profile management |
| `ROLE_ADMIN` | All user data + user management (list, reset password, activate/deactivate, delete) |

## Admin User

- **Email**: `sampath12082@gmail.com`
- **Default Password**: `Admin@123` (configurable via `ADMIN_DEFAULT_PASSWORD` env var)
- Seeded automatically on first startup by `AdminUserSeeder`
- All existing portfolio data (stocks, holdings, transactions, MF) assigned to this user
- Cannot be deleted via admin panel

---

## Authentication Flow

### Registration
```
User submits email + password + name
  → Backend creates user (email_verified=false)
  → Sends 6-digit OTP to email (10-min expiry)
  → User enters OTP
  → Backend sets email_verified=true
  → User can now login
```

### Login
```
User submits email + password
  → Backend validates credentials (BCrypt)
  → Checks account status (ACTIVE/INACTIVE/SUSPENDED)
  → Returns JWT access token (15min) + refresh token (7 days)
  → Frontend stores tokens in localStorage
  → Axios interceptor attaches Bearer token to all /api/* requests
```

### Token Refresh
```
Access token expires (15min)
  → Frontend gets 401
  → Sends refresh token to /api/auth/refresh
  → Backend returns new access token
  → If refresh token also expired → redirect to /login
```

### Forgot Password
```
User enters email → /api/auth/forgot-password
  → Backend sends OTP to email (if account exists)
  → User enters OTP + new password → /api/auth/reset-password
  → Backend validates OTP, updates password hash
```

### Change Password
```
Authenticated user → /api/auth/change-password
  → Requires current password + new password
  → Backend validates current password, updates hash
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
| POST | `/api/auth/register` | `{email, password, firstName, lastName?}` | `UserResponse` (201) |
| POST | `/api/auth/verify-otp` | `{email, otpCode}` | `{message}` |
| POST | `/api/auth/login` | `{email, password}` | `{accessToken, refreshToken, user}` |
| POST | `/api/auth/forgot-password` | `{email}` | `{message}` |
| POST | `/api/auth/reset-password` | `{email, otpCode, newPassword}` | `{message}` |
| POST | `/api/auth/refresh` | `{refreshToken}` | `{accessToken, refreshToken, user}` |

### Authenticated

| Method | Endpoint | Request Body | Response |
|--------|----------|-------------|----------|
| POST | `/api/auth/change-password` | `{currentPassword, newPassword}` | `{message}` |
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
- Email + password form
- Error messages for invalid credentials
- Links to Register and Forgot Password
- Redirects to Dashboard on success

### Register (`/register`)
- Email + first name + last name + password form
- On submit → shows OTP verification step
- 6-digit OTP input with tracking-widest styling
- On verify → redirects to Login

### Forgot Password (`/forgot-password`)
- Step 1: Enter email → sends OTP
- Step 2: Enter OTP + new password → resets
- Redirects to Login on success

### Profile (`/profile`)
- **Personal Details** card: email (read-only), first name, last name, phone, role
- **Change Password** card: current password + new password
- Save button updates profile via PUT /api/profile

### Header
- Shows user's first name with User icon
- Profile link navigates to /profile
- Red Logout button clears tokens and redirects to /login

---

## Security Architecture

### Request Flow
```
Browser → /api/* request
  → Axios interceptor adds Authorization: Bearer <token>
  → Spring Security JwtAuthenticationFilter
    → Extract token from header
    → Validate signature + expiry via JwtService
    → Load UserDetails from DB
    → Set SecurityContext
  → Controller executes
```

### Endpoint Protection
```
/api/auth/**     → permitAll (public)
/api/admin/**    → hasAuthority('ROLE_ADMIN')
/api/**          → authenticated
/**              → permitAll (static files, SPA routes)
```

### On 401 Response
```
Axios response interceptor catches 401
  → Clears localStorage tokens
  → Redirects to /login (if not already there)
```

---

## Backend Files

| File | Purpose |
|------|---------|
| `entity/User.java` | User JPA entity |
| `entity/OtpToken.java` | OTP token entity |
| `repository/UserRepository.java` | findByEmail, existsByEmail |
| `repository/OtpTokenRepository.java` | Find latest unused OTP |
| `service/AuthService.java` | Auth interface |
| `service/impl/AuthServiceImpl.java` | Register, login, OTP, password management |
| `service/JwtService.java` | JWT interface |
| `service/impl/JwtServiceImpl.java` | Token generation/validation (jjwt 0.12.6) |
| `service/EmailService.java` | Email interface |
| `service/impl/EmailServiceImpl.java` | OTP email via Spring Mail |
| `controller/AuthController.java` | /api/auth/* endpoints |
| `controller/AdminController.java` | /api/admin/* endpoints |
| `controller/ProfileController.java` | /api/profile endpoints |
| `security/JwtAuthenticationFilter.java` | OncePerRequestFilter — JWT validation |
| `security/JwtAuthEntryPoint.java` | 401 response handler |
| `security/UserDetailsServiceImpl.java` | Load user from DB for Spring Security |
| `config/SecurityConfig.java` | JWT filter chain, BCrypt, endpoint rules |
| `config/AdminUserSeeder.java` | Seeds admin user on first startup |

## Frontend Files

| File | Purpose |
|------|---------|
| `auth/AuthContext.tsx` | React context — token state, login/logout functions |
| `auth/AuthGuard.tsx` | Route guards (AuthGuard, AdminGuard) |
| `auth/types.ts` | UserResponse, AuthResponse types |
| `api/client.ts` | Axios interceptors (request: add token, response: 401 → /login) |
| `pages/LoginPage.tsx` | Login form |
| `pages/RegisterPage.tsx` | Register + OTP verification |
| `pages/ForgotPasswordPage.tsx` | Forgot password + OTP reset |
| `pages/ProfilePage.tsx` | Profile edit + change password |

---

## Environment Variables

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `JWT_SECRET` | Yes | dev fallback | Base64-encoded HMAC signing key |
| `JWT_EXPIRATION` | No | `900000` (15min) | Access token TTL in ms |
| `JWT_REFRESH_EXPIRATION` | No | `604800000` (7d) | Refresh token TTL in ms |
| `ADMIN_DEFAULT_PASSWORD` | No | `Admin@123` | Initial admin password |
| `SPRING_MAIL_HOST` | For OTP | `smtp.gmail.com` | SMTP server |
| `SPRING_MAIL_PORT` | For OTP | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | For OTP | — | SMTP username |
| `SPRING_MAIL_PASSWORD` | For OTP | — | SMTP app password |

---

## Flyway Migrations

| Version | Purpose |
|---------|---------|
| V15 | Create `users` table |
| V16 | Create `otp_tokens` table |
| V17 | Add `user_id` FK to all domain tables |
| V18 | Assign existing data to admin user (user_id=1) |
