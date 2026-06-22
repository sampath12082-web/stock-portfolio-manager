---
name: project-review
description: Project-specific technical review. Use when asked to do a project review, check project quality, audit project security, or inspect the full codebase.
---

# Technical Code Review

Performs a structured code review of changed or specified files. Checks for correctness bugs, security issues, performance problems, and code quality.

## When to Use

- Before committing significant changes
- When asked to "review code", "check for bugs", "audit security"
- After implementing a new feature
- Before merging a PR

## Review Process

### Step 1: Identify Scope

```bash
# Uncommitted changes
git diff --stat HEAD

# Changes on current branch vs main
git diff --stat origin/main...HEAD

# Specific files
git diff --stat HEAD -- backend/src/ frontend/src/
```

### Step 2: Run Automated Checks

```bash
# Backend compile check
cd backend && ./mvnw compile -q 2>&1 | tail -5

# Frontend type check + build
cd frontend && npm run build 2>&1 | grep -E "error|warning|✓"

# E2E tests (requires backend running)
cd e2e && npx playwright test 2>&1 | tail -5
```

### Step 3: Manual Review Checklist

#### Correctness
- [ ] Does the code do what the enhancement/bug description says?
- [ ] Are edge cases handled (null, empty, zero, negative)?
- [ ] Do new endpoints return correct HTTP status codes?
- [ ] Are error messages user-friendly and not leaking internals?
- [ ] Do database queries filter by `user_id` for multi-tenancy?

#### Security
- [ ] No hardcoded secrets (passwords, API keys, tokens)
- [ ] Passwords hashed with BCrypt (never stored or logged plain)
- [ ] JWT token validated on all protected endpoints
- [ ] Admin endpoints check `ROLE_ADMIN` authority
- [ ] SQL injection safe (using JPA parameterized queries)
- [ ] No XSS vectors in user-provided content rendered in frontend
- [ ] Sensitive data not logged (grep for `log.*password|log.*token|log.*secret`)

```bash
# Quick secrets scan
grep -rn --include="*.java" --include="*.ts" --include="*.yml" -iE "(password|secret|token)\s*[:=]\s*[\"'][^${\"]" backend/src/ frontend/src/ | grep -v "test" | grep -v ".class"
```

#### Performance
- [ ] No N+1 query patterns (fetching related entities in loops)
- [ ] Large collections use pagination or `.stream()` with filters
- [ ] No unnecessary `findAll()` when filtered query exists
- [ ] Frontend doesn't fetch data on every render (uses TanStack Query caching)
- [ ] No synchronous blocking calls on the main thread

#### Code Quality
- [ ] Constructor injection (no `@Autowired`)
- [ ] DTOs are Java records (immutable)
- [ ] Entities use manual getters/setters (project convention — no Lombok)
- [ ] Frontend hooks follow `useXxx` naming pattern
- [ ] No commented-out code blocks
- [ ] No `console.log` in production code
- [ ] No unused imports

```bash
# Find console.log in frontend
grep -rn "console\.\(log\|warn\|error\)" frontend/src/ --include="*.ts" --include="*.tsx" | grep -v node_modules

# Find unused imports (IDE diagnostics)
cd backend && ./mvnw compile 2>&1 | grep "unused"
```

#### Database
- [ ] Schema changes go through Flyway (never `ddl-auto=update`)
- [ ] New columns have sensible defaults or are nullable
- [ ] Foreign key constraints exist where needed
- [ ] Migration files are numbered sequentially (V15, V16, ...)

```bash
# List migrations
ls -1 backend/src/main/resources/db/migration/
```

#### Frontend
- [ ] All React hooks called before any early return (Rules of Hooks)
- [ ] No state mutations during render
- [ ] TanStack Query invalidation chains correct after mutations
- [ ] Loading states handled (show spinner, not blank page)
- [ ] Error states handled (show message, not crash)
- [ ] Responsive design (works on mobile width)

#### API Contract
- [ ] Request/response DTOs match between backend and frontend `types.ts`
- [ ] New backend fields added to frontend type definitions
- [ ] Frontend handles null/optional fields gracefully

```bash
# Compare backend response fields with frontend types
grep -A 20 "record DashboardResponse" backend/src/main/java/com/stocks/myportfolio/dto/response/DashboardResponse.java
grep -A 10 "interface DashboardResponse" frontend/src/api/types.ts
```

### Step 4: Report Findings

Format findings as:

```
## Code Review: <scope>

### Critical (must fix)
- [file:line] Description of issue

### Warnings (should fix)
- [file:line] Description of concern

### Suggestions (nice to have)
- [file:line] Improvement idea

### Passed
- Security: no secrets, JWT validation correct
- Types: backend/frontend DTOs match
- Tests: 61/61 passing
```

## Review Specific Areas

### Review auth changes
```bash
grep -rn "permitAll\|authenticated\|hasAuthority" backend/src/main/java/com/stocks/myportfolio/config/SecurityConfig.java
grep -rn "Authorization" frontend/src/api/client.ts
```

### Review new endpoints
```bash
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" backend/src/main/java/com/stocks/myportfolio/controller/ | grep -v ".class"
```

### Review frontend components
```bash
grep -rn "useState\|useEffect\|useQuery\|useMutation" frontend/src/pages/*.tsx | wc -l
```
