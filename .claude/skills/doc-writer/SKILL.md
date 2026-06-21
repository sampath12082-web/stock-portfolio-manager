---
name: doc-writer
description: Audit and sync all documentation with actual app functionality. Use when asked to update all docs, sync docs with code, ensure docs are current, or write missing documentation.
---

# Document Writer

Audits the entire documentation set against the running application and codebase, identifies gaps and stale content, then updates all docs to match reality. Unlike `maintain-docs` (which provides rules), this skill **does the work** — reads code, queries APIs, and rewrites docs.

## When to Use

- After a batch of features is implemented
- When docs feel "out of date"
- Before a handoff or release
- When asked "are the docs current?"

## Full Audit Process

### Step 1: Inventory Current State

```bash
# Backend: count entities, controllers, services, migrations
echo "=== Backend ===" 
echo "Entities: $(find backend/src -name "*.java" -path "*/entity/*" | wc -l)"
echo "Controllers: $(find backend/src -name "*Controller.java" | wc -l)"
echo "Services: $(find backend/src -name "*ServiceImpl.java" | wc -l)"
echo "Migrations: $(ls backend/src/main/resources/db/migration/*.sql | wc -l)"

# Frontend: count pages, components, hooks
echo "=== Frontend ==="
echo "Pages: $(ls frontend/src/pages/*.tsx | wc -l)"
echo "Components: $(find frontend/src/components -name "*.tsx" | wc -l)"
echo "Hooks: $(ls frontend/src/hooks/*.ts | wc -l)"

# Tests
echo "=== Tests ==="
echo "Test files: $(ls e2e/tests/*.spec.ts | wc -l)"

# Docs
echo "=== Docs ==="
ls -1 docs/
```

### Step 2: Query Live App for Actual Data

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sampath12082@gmail.com","password":"Admin@123"}' \
  | python -c "import json,sys; print(json.load(sys.stdin)['accessToken'])")

echo "=== Data Counts ==="
curl -s http://localhost:8081/api/stocks -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Stocks: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/holdings -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; h=json.load(sys.stdin); print(f'Holdings: {len(h)} (active: {sum(1 for x in h if x[\"quantity\"]>0)})')"
curl -s http://localhost:8081/api/transactions -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Transactions: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/mf/funds -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'MF Funds: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/mf/holdings -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'MF Holdings: {len(json.load(sys.stdin))}')"
curl -s http://localhost:8081/api/admin/users -H "Authorization: Bearer $TOKEN" | python -c "import json,sys; print(f'Users: {len(json.load(sys.stdin))}')"

echo "=== Test Results ==="
cd e2e && npx playwright test 2>&1 | grep -E "passed|failed" | tail -1
```

### Step 3: Audit Each Document

For each doc file, read it and compare against code/API reality:

#### CLAUDE.md (repo root)
Check against:
```bash
# Build commands still work?
cat CLAUDE.md | grep "```" -A 1 | grep -v "```"

# Project structure matches?
ls -d backend/ frontend/ e2e/ scripts/ docs/

# Migration count current?
ls backend/src/main/resources/db/migration/*.sql | wc -l
```

**Update if**: build commands changed, structure changed, new modules added, migration count wrong.

#### docs/architecture.md
Check against:
```bash
# Security config matches?
grep -A 5 "authorizeHttpRequests" backend/src/main/java/com/stocks/myportfolio/config/SecurityConfig.java

# External integrations list current?
grep -rn "RestClient\|RestTemplate\|WebClient" backend/src/main/java/ | grep -v ".class" | grep -v test
```

**Update if**: new integrations, security rules changed, architecture diagram outdated.

#### docs/api-reference.md
Check against:
```bash
# Extract ALL endpoints from controllers
grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/ | \
  grep -oP '"[^"]*"' | tr -d '"' | sort -u

# Extract ALL documented endpoints
grep -oP '`/api/[^`]*`' docs/api-reference.md | tr -d '`' | sort -u

# Find undocumented endpoints
comm -23 \
  <(grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/ | grep -oP '"[^"]*"' | tr -d '"' | sort -u) \
  <(grep -oP '`/api/[^`]*`' docs/api-reference.md | tr -d '`' | sort -u)
```

**Update if**: new endpoints exist in code but not in docs.

#### docs/features.md
Check against:
```bash
# Pages in router vs documented pages
grep "path=" frontend/src/App.tsx | grep -oP '"[^"]*"'
grep "###.*\`/" docs/features.md
```

**Update if**: new pages added, column orders changed, new features on existing pages.

#### docs/user-module.md
Check against:
```bash
# Auth endpoints in code vs documented
grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/AuthController.java | grep -oP '"[^"]*"'
grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/AdminController.java | grep -oP '"[^"]*"'
grep -rn "@.*Mapping" backend/src/main/java/com/stocks/myportfolio/controller/ProfileController.java | grep -oP '"[^"]*"'

# User entity fields vs documented
grep "private " backend/src/main/java/com/stocks/myportfolio/entity/User.java
```

**Update if**: new auth flows, entity fields changed, password policy changed, security questions added.

#### docs/BUGS.md
Check against:
```bash
# Open bugs that might be fixed
grep "Open" docs/BUGS.md

# Check if "fixed" bugs are actually working
cd e2e && npx playwright test 2>&1 | tail -3
```

**Update if**: bugs fixed but still marked open, new bugs found.

#### docs/ENHANCEMENTS.md
Check against:
```bash
# Enhancements marked open
grep -c "Open\|open" docs/ENHANCEMENTS.md
# Enhancements marked done
grep -c "Done\|done\|Resolved" docs/ENHANCEMENTS.md
```

**Update if**: statuses changed, new enhancements needed.

#### docs/HANDOFF.md
**Always rewrite** with fresh data from Step 2. This is the most time-sensitive doc.

### Step 4: Fix Every Gap Found

For each gap identified:
1. Read the relevant source code
2. Query the API to verify current behavior
3. Update the doc file with accurate information
4. Use exact numbers from live queries (not memory)

### Step 5: Verify Updates

```bash
# Consistency checks
echo "=== Migration count ===" 
echo "CLAUDE.md says: $(grep -oP 'V1[–-]V\d+' CLAUDE.md)"
echo "Actual: $(ls backend/src/main/resources/db/migration/*.sql | wc -l) files"

echo "=== Open bugs ==="
grep "Open" docs/BUGS.md | wc -l

echo "=== Open enhancements ==="  
grep -c "Open\|open\|Pending\|pending" docs/ENHANCEMENTS.md

echo "=== HANDOFF pending section ==="
grep -A 1 "Pending" docs/HANDOFF.md | head -5

echo "=== Test count ==="
cd e2e && npx playwright test --list 2>&1 | tail -1
```

### Step 6: Commit

```bash
git add docs/ CLAUDE.md
git commit -m "Sync documentation with current app state

Updated: <list changed files>
- <what changed in each>"
```

## Document Templates

### New Feature Documentation
When a new feature is implemented, update these files in order:
1. `docs/ENHANCEMENTS.md` — mark as Done with resolution
2. `docs/features.md` — add feature description under relevant page
3. `docs/api-reference.md` — add new endpoints
4. `docs/architecture.md` — if new module/integration
5. `docs/user-module.md` — if auth-related
6. `CLAUDE.md` — if build/structure changed
7. `docs/HANDOFF.md` — update current state

### New Bug Documentation
1. `docs/BUGS.md` — add with severity, root cause
2. After fix: move to resolved, update `docs/HANDOFF.md`

## Quality Standards

- **Numbers come from live queries**, not from memory or previous docs
- **Every command in docs must work** — run it before writing it
- **Column orders in features.md must match actual UI** — read the TSX
- **API fields in api-reference.md must match DTOs** — read the Java records
- **Status in ENHANCEMENTS.md must reflect reality** — check the code
- **HANDOFF.md is rewritten, not patched** — stale data accumulates
