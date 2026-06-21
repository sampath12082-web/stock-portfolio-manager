---
name: maintain-docs
description: Update, review, and maintain project documentation. Use when asked to update docs, maintain documentation, review handoff, sync tracking files, or document a new feature/bug/enhancement.
---

# Maintain Documentation

This skill governs how documentation is updated and kept in sync across the project. All docs live in `docs/`. `CLAUDE.md` stays at repo root.

## Documentation Inventory

| File | Purpose | When to Update |
|------|---------|----------------|
| `docs/HANDOFF.md` | Session continuity — current state, pending work, test results | **Every session end**, after any deploy, after data changes |
| `docs/BUGS.md` | Bug tracking with severity, root cause, resolution | When bugs are found, investigated, or fixed |
| `docs/ENHANCEMENTS.md` | Feature/enhancement tracking with priority and status | Before starting new features, after completing them |
| `docs/architecture.md` | System diagram, project structure, security flow | When new modules/integrations are added |
| `docs/api-reference.md` | REST API endpoint reference | When endpoints are added/changed/removed |
| `docs/features.md` | Feature documentation for all pages | When UI features change |
| `docs/user-module.md` | Auth, security questions, password policy, RSA encryption | When auth/user features change |
| `CLAUDE.md` | Claude Code guidance — build commands, architecture overview | When project structure or build process changes |

## Rules

### Before Any Code Change
1. **New feature?** → Add to `docs/ENHANCEMENTS.md` with number, priority, status "Open"
2. **Bug found?** → Add to `docs/BUGS.md` with severity, root cause, status "Open"
3. **Never implement without logging first**

### After Code Changes
1. **Bug fixed?** → Move from Open to Resolved in `docs/BUGS.md` with resolution and date
2. **Enhancement done?** → Update status in `docs/ENHANCEMENTS.md` (e.g., "Backend done", "Done")
3. **New API endpoint?** → Add to `docs/api-reference.md`
4. **UI changed?** → Update `docs/features.md`
5. **Auth/user changed?** → Update `docs/user-module.md`
6. **Build/structure changed?** → Update `CLAUDE.md`

### Session End (Always)
1. Update `docs/HANDOFF.md` with:
   - Current state (what's running, DB version, data counts)
   - Pending work (open bugs, open enhancements with status)
   - Test results (suite counts, pass/fail)
   - Application URL

## How to Update Each File

### HANDOFF.md

Read current state from APIs and git, then rewrite:

```bash
# Get current data counts
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

curl -s http://localhost:8081/api/dashboard -H "Authorization: Bearer $TOKEN" | python -m json.tool
curl -s http://localhost:8081/api/stocks -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Stocks: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/holdings -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; h=json.load(sys.stdin); print(f'Holdings: {len(h)} (active: {sum(1 for x in h if x[\"quantity\"]>0)})')"
curl -s http://localhost:8081/api/transactions -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Transactions: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/mf/holdings -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'MF Holdings: {len(json.load(sys.stdin))}')"

# Get test results
cd e2e && npx playwright test 2>&1 | tail -3

# Get migration count
ls backend/src/main/resources/db/migration/*.sql | wc -l

# Get open bugs and enhancements
grep -c "Open" docs/BUGS.md
grep -c "Open\|Pending\|open\|pending" docs/ENHANCEMENTS.md
```

### BUGS.md

**Adding a bug:**
```markdown
| # | Severity | Bug | Root Cause | Status |
| 21 | High | Description of what's broken | Why it's broken — file, line, logic error | Open |
```

Severity: `Critical` (app broken), `High` (feature broken), `Medium` (cosmetic/misleading), `Low` (minor)

**Resolving a bug:**
Move row from Open to Resolved table, add resolution and date.

### ENHANCEMENTS.md

**Adding an enhancement:**
```markdown
| # | Type | Module | Description | Priority | Status |
| 34 | Feature | Auth | Password policy: 16-20 chars, complexity rules | P1 | Open |
```

Priority: `P1` (must have), `P2` (should have), `P3` (nice to have)
Status: `Open`, `Backend done`, `Frontend pending`, `Done`

### api-reference.md

Group by domain. Include auth requirement. Format:
```markdown
## Domain APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/resource` | List all resources |
| POST | `/api/resource` | Create resource |
```

Note auth at top: "All `/api/*` require JWT except `/api/auth/*`"

### features.md

One section per page. Document:
- What the page shows (sections, cards, tables)
- Interactive features (filters, sorting, modals)
- Color coding rules
- Column order

### user-module.md

Comprehensive auth documentation:
- Auth flows (register → OTP → login → refresh → logout)
- Password policy with regex
- Security questions
- Network encryption (RSA)
- API endpoints with request/response bodies
- Data model (tables, columns, constraints)
- Frontend and backend file index

## Verification

After updating docs, verify consistency:

```bash
# Check no stale "Open" items that are actually done
grep "Open" docs/BUGS.md
grep "Open" docs/ENHANCEMENTS.md

# Check HANDOFF.md migration count matches actual
ls backend/src/main/resources/db/migration/*.sql | wc -l

# Check api-reference.md covers all controllers
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" backend/src/main/java/com/stocks/myportfolio/controller/ | wc -l

# Check features.md mentions all pages
grep "###.*\`/" docs/features.md
```

## Common Mistakes to Avoid

- **Don't say "No pending work" when BUGS.md has open items**
- **Don't update HANDOFF.md without checking BUGS.md and ENHANCEMENTS.md first**
- **Don't add new API endpoints without updating api-reference.md**
- **Don't implement features without adding to ENHANCEMENTS.md first**
- **Don't resolve a bug without adding the resolution text**
- **Don't use stale data counts — always query the live API**
- **Don't forget to update test counts after adding/removing tests**
