# AGENTS — Running Mamute locally

Mamute is a Stack Overflow-style Q&A web application. Stack: VRaptor 4 + Weld CDI +
Hibernate + JSP, Java 7 source/target, embedded Jetty 8 via
`br.com.caelum.vraptor.server.Main`.

---

## 0. Prerequisites (absolute paths on this machine)

| Thing | Path / command | Notes |
|---|---|---|
| **JDK 8** (mandatory) | `/usr/lib/jvm/jdk-8.0.202-oracle-x64` | Java 7 source/target does not compile on JDK 9+. Never inherit the shell `JAVA_HOME` (machine default is JDK 21). |
| **Maven** | on `PATH` | |
| **MySQL 5.7** | podman/docker | Runtime needs MySQL; only MySQL configs exist in main scope. |
| **Node.js + npm** | on `PATH` (nvm) | Only needed once, to generate the web assets with grunt. |

> ⚠️ **Never inherit the shell `JAVA_HOME`.** Always export the JDK 8 path explicitly.

---

## 1. Quick start (copy-paste)

```bash
export JDK8=/usr/lib/jvm/jdk-8.0.202-oracle-x64
cd path/to/mamute

# --- 1. MySQL (rootless podman; docker needs sudo on this machine) -----------
podman start mamute-mysql 2>/dev/null || podman run -d --name mamute-mysql \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=true -p 3306:3306 docker.io/library/mysql:5.7
# MUST be 5.7: the pom's mysql-connector 5.1.19 cannot do MySQL 8 auth.
# (short image name fails: use the fully qualified docker.io/library/mysql:5.7)

# --- 2. Compile (JDK 8) ------------------------------------------------------
JAVA_HOME=$JDK8 mvn -B -q compile
# output dir is src/main/webapp/WEB-INF/classes/ (set in the pom)

# --- 3. Web assets (only needed once, or after a clean clone) ----------------
npm install
./node_modules/grunt-cli/bin/grunt build     # generates webapp/{css,js,imgs,font}

# --- 4. Run (embedded Jetty 8, dev mode) -------------------------------------
JAVA_HOME=$JDK8 nohup mvn -B exec:java \
  -Dexec.mainClass=br.com.caelum.vraptor.server.Main \
  -Dexec.args="not-grunt" -Dserver.port=8088 > /tmp/mamute.log 2>&1 &
# startup ~8 s; ~200 routes logged. Logs: /tmp/mamute.log

# --- 5. Use it ---------------------------------------------------------------
# http://localhost:8088/          (home; fresh DB shows "No questions found!")
# http://localhost:8088/sign-up   (email sign-up works out of the box)
# http://localhost:8088/login

# --- 6. Stop -----------------------------------------------------------------
pkill -f 'exec:java'
```

Port 8080 is often already taken on a dev box; the runbook uses **8088**
(`-Dserver.port`, or env `PORT`).

---

## 2. Database

- Dev config: `src/main/resources/development/hibernate.cfg.xml` → DB `mamute_development`,
  user `root`, blank password, `createDatabaseIfNotExist=true` (schema auto-created on first
  boot, incl. a seeded `moderator@mamuteqa.org` user).
- VRaptor's `MamuteEnvironment` loads `/environment.properties` + `/<env>.properties`
  (e.g. `development.properties`), falling back to `/mamute.properties`. Env-file values win.
- **`development.properties` is GITIGNORED** — it holds local secrets (OAuth credentials)
  and the port override. Copy the tracked template to create it:
  `cp src/main/resources/development.properties.example src/main/resources/development.properties`
- Its `${VAR}` env-var placeholder support is broken (`System.getenv("${VAR}")` with braces
  → always null) — don't rely on it.

## 3. OAuth sign-in

- Keys are read by `OAuthServiceCreator`: `google.client_id`, `google.client_secret`,
  `google.redirect_uri`, `facebook.app_id`, `facebook.app_secret`, `facebook.redirect_uri`.
  Callback = `env.get("host") + redirect_uri` — so the `host` property in
  `development.properties` must match the port you run on (it overrides mamute.properties'
  `host=http://localhost:8080`, which otherwise bounces redirects to the wrong port).
- **Google**: works. Register redirect URI `http://localhost:8088/sign-up/google` in GCP.
  The profile is fetched from `oauth2/v3/userinfo` (Google+ API is dead),
  token exchanged manually against `oauth2.googleapis.com/token` (Scribe 1.3.5 can't parse
  Google's JSON token response), profile request signed with `Authorization: Bearer`.
  Callback route accepts both `/sign-up/google` and `/sign-up/google/`.
- **Facebook**: sign-up/login button was REMOVED (no working credentials, flow unmaintained).
  The controller/DAO code is still there; re-adding the button means restoring the
  `feature.facebook.login` block in `src/main/webapp/WEB-INF/tags/socialLoginMethods.tag`.
- gcloud **cannot** create OAuth web clients (UI-only) — don't try.

## 4. Assets & the grunt gotchas

- The repo tracks only the **sources** (`src/main/webapp/assets/` LESS/JS + dev-referencing
  templates). Built outputs (`webapp/{css,js,imgs,font}`, `assets/generated-css`) are
  **gitignored** — a fresh clone renders unstyled until step 1.3 above is run once.
- `grunt build` dirties the working tree **by design**: it deletes `src/main/webapp/assets/`
  (155 files), rewrites `header.tag`/`coda.jspf`/`showQuestion.jsp`/`terms.jspf` to hashed
  refs, and `filerev` renames the deps to hashed names.
- To get back to a **clean tree with the app still styled**:
  ```bash
  git restore . && ./node_modules/grunt-cli/bin/grunt less && ./node_modules/grunt-cli/bin/grunt copy:ignored
  ```
  (`less` regenerates the gitignored `assets/generated-css/` the dev templates reference;
  `copy:ignored` re-copies the UNHASHED deps into `css/deps` + `font` — without them the
  icon font 405s and the Google logo vanishes from the social buttons.)
- **405 root cause**: VRaptor registers a catch-all `/*` [OPTIONS] route (CORS), so any GET
  to a path with no route and no file → `405 Method Not Allowed` instead of 404. Missing
  static files always look like 405s in the console. Benign known one:
  `/css/deps/custom.css` (referenced by templates, never generated by any task).

## 5. Logs & ports

| What | Where |
|---|---|
| App | `http://localhost:8088/` |
| Server log | `/tmp/mamute.log` (or the terminal running `exec:java`) |
| MySQL | `podman exec mamute-mysql mysql -uroot mamute_development` |

## 6. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| Every URL 404s / compile errors about Java version | Running on JDK 9+. Restart with `JAVA_HOME=$JDK8`. |
| Page unstyled, flood of 405s | Assets not generated → run step 1.3 (`npm install` + `grunt build`). |
| Icons/logo missing from buttons | Unhashed deps missing → `grunt copy:ignored`. |
| Redirects (or OAuth callback) go to port 8080 | `host` in `development.properties` still the mamute.properties default → set it to your port. |
| `401 invalid_client` on accounts.google.com | Placeholder/rotated Google credentials in `development.properties`. |
| Signup 500 with `Can't extract a token` | Old Scribe path — fixed in this repo; make sure you're on latest `main`. |
| `Table 'mamute_development' doesn't exist` / connection refused | MySQL container not running → step 1.1. |
