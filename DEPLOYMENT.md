# Deploying WolfCafe on Proxmox

This walks through running WolfCafe on a Proxmox VE host and publishing it with
a Cloudflare Tunnel.

Everything runs as three Docker containers defined in
[`docker-compose.yml`](docker-compose.yml). You do not install Java, Node, or
MySQL on the guest — Docker builds and runs all of it.

---

## What you are deploying

```
                     ┌─────────────────────────────────────────┐
  Cloudflare Tunnel  │  Proxmox guest (VM or LXC)              │
  ────────────────►  │                                         │
                     │   frontend    nginx :80                 │
                     │      │        serves the built React    │
                     │      │        app, proxies /api ────┐   │
                     │      │                              │   │
                     │   backend     Spring Boot :8080  ◄──┘   │
                     │      │                                  │
                     │      ▼                                  │
                     │   db          MySQL 8.4                 │
                     │               (docker volume db-data)   │
                     └─────────────────────────────────────────┘
```

The browser only ever talks to `frontend`. nginx serves the app and
reverse-proxies `/api` to the backend, so every request is same-origin. That is
why the same image works on `localhost`, on a LAN IP, and on your tunnel
hostname with no rebuild — nothing anywhere has a hostname compiled into it.

`db` and `backend` are not published to the host at all. Only the frontend port
is reachable from outside the Docker network.

---

## Sizing the guest

Measured from a running instance of this stack:

| | Idle memory | Image size |
| --- | --- | --- |
| `db` (MySQL 8.4) | ~480 MB | 1.12 GB |
| `backend` (Spring Boot) | ~300 MB | 418 MB |
| `frontend` (nginx) | ~10 MB | 78 MB |
| **Total** | **~790 MB** | **~1.6 GB** |

The database volume starts around 220 MB. The Maven build inside Docker is the
heaviest step — it produces roughly 2.2 GB of build cache and wants noticeably
more RAM than the app does at rest.

**Recommended guest:**

| Resource | Minimum | Comfortable |
| --- | --- | --- |
| vCPU | 2 | 2–4 |
| RAM | 2 GB | **4 GB** |
| Disk | 16 GB | 32 GB |

2 GB will run the app fine but the first `docker compose build` may be slow or
get OOM-killed. If you are tight on RAM, see
[Building elsewhere](#if-the-build-is-too-heavy-for-the-guest).

---

## Step 1 — Create the guest

You have two options. **If you are not sure, use a VM.**

### Option A: a VM (recommended)

Docker is fully supported inside a VM with no special configuration.

1. Download a Debian 12 or Ubuntu Server 24.04 ISO to your Proxmox ISO storage.
2. **Create VM**, and give it the resources from the table above.
3. Install the OS as normal, and install the QEMU guest agent inside it
   (`apt install qemu-guest-agent`) so Proxmox can report its IP and shut it
   down cleanly.
4. In **Options → Start at boot**, enable it.

### Option B: an LXC container (lighter, more fiddly)

An LXC uses less RAM and disk, but Docker inside LXC needs two features turned
on, and Proxmox does not officially support the combination.

1. **Create CT** from a Debian 12 template. Uncheck *Unprivileged* only if you
   hit problems you cannot solve — an unprivileged container is safer.
2. Before starting it: **Options → Features**, enable **Nesting** and
   **keyctl**. Docker will not start without these.
3. **Options → Start at boot**, enable it.

> If Docker fails to start in an LXC with cgroup or overlay errors, the fastest
> fix is to rebuild as a VM rather than debug it.

---

## Step 2 — Install Docker

Inside the guest, as root:

```bash
apt update && apt install -y curl git ca-certificates
```

Docker's official installer sets up the engine, the Compose plugin, and the
systemd service in one step:

```bash
curl -fsSL https://get.docker.com | sh
```

Confirm it works and will come back after a reboot:

```bash
docker run --rm hello-world && systemctl is-enabled docker
```

---

## Step 3 — Get the code

The repository is private, so you need credentials on the guest. A read-only
**deploy key** is the cleanest option:

```bash
ssh-keygen -t ed25519 -C "proxmox-wolfcafe" -f ~/.ssh/id_ed25519 -N ""
cat ~/.ssh/id_ed25519.pub
```

Add that public key to the repo on GitHub under **Settings → Deploy keys**
(read access is enough), then clone:

```bash
git clone git@github.com:ncstate-csc-coursework/csc326-2026-spring-tp-212-3.git
```

```bash
cd csc326-2026-spring-tp-212-3
```

---

## Step 4 — Configure secrets

`.env` holds every deployment secret and is gitignored. Start from the
documented template:

```bash
cp .env.example .env
```

Generate real values rather than inventing them:

```bash
echo "DB_PASSWORD=$(openssl rand -hex 24)"; echo "JWT_SECRET=$(openssl rand -hex 32)"
```

Then edit `.env` and set, at minimum:

| Variable | What to put |
| --- | --- |
| `DB_PASSWORD` | The generated value. MySQL root password. |
| `JWT_SECRET` | The generated value. Signs login tokens. |
| `DEFAULT_PASSWORD` | Password for the seeded `admin`, `staff`, and `customer` accounts. **Change this** — the usernames are public knowledge. |
| `DB_RESET_ZONE` | Your timezone, e.g. `America/New_York`. Controls when the nightly reset fires. |
| `WEB_PORT` | Host port for the site. See the tunnel section below. |

Compose refuses to start if `DB_PASSWORD`, `JWT_SECRET`, or `DEFAULT_PASSWORD`
are missing, so you cannot accidentally deploy with blanks.

> Do not `source .env` in a shell. `DB_RESET_CRON=0 0 0 * * *` contains spaces
> and the shell will mangle it. Docker Compose parses the file itself and
> handles it correctly.

---

## Step 5 — First run

```bash
docker compose up -d --build
```

The first build takes several minutes — it downloads the Maven and Node base
images and compiles the backend from source. Later builds reuse the cache.

Watch it come up:

```bash
docker compose ps
```

All three services should reach `healthy`. If `backend` sits in `starting` for
more than a couple of minutes, check `docker compose logs backend`.

Verify locally on the guest before involving Cloudflare:

```bash
curl -I http://localhost:8080/
```

(Use whatever you set `WEB_PORT` to.) You want `HTTP/1.1 200 OK`.

---

## Step 6 — Publish with a Cloudflare Tunnel

Two ways. The sidecar is simpler and does not require publishing any port on
the guest.

### Option A: cloudflared as a sidecar (recommended)

The compose file already contains a `cloudflared` service behind a `tunnel`
profile, so it only runs when you ask for it.

1. In the Cloudflare dashboard: **Zero Trust → Networks → Tunnels → Create a
   tunnel**, pick **Cloudflared**, and name it.
2. Copy the tunnel **token** from the install command Cloudflare shows you.
3. Add it to `.env`:

   ```bash
   echo "TUNNEL_TOKEN=paste-the-token-here" >> .env
   ```

4. Under **Public Hostnames**, add your hostname and point it at:

   - Type: `HTTP`
   - URL: `frontend:80`

   That is the Docker service name. The tunnel container is on the same Docker
   network, so it resolves without any host ports at all.

5. Start it:

   ```bash
   docker compose --profile tunnel up -d
   ```

Because nothing needs to reach the guest directly now, close the published port
by setting it to loopback only in `.env`:

```bash
WEB_PORT=127.0.0.1:8080
```

Then `docker compose up -d frontend`. The site stays reachable through the
tunnel, but nothing on your LAN can hit it.

### Option B: cloudflared on the guest

If you would rather run the tunnel outside Docker, install `cloudflared` on the
guest per Cloudflare's instructions and point the public hostname at
`http://localhost:8080` (your `WEB_PORT`). Leave `WEB_PORT` bound normally in
that case.

---

## Step 7 — Lock it down

The app has no rate limiting and its default usernames are documented in this
repo. Before leaving it exposed:

- **Change `DEFAULT_PASSWORD`.** `admin` / `staff` / `customer` with a guessable
  password on a public hostname is the single biggest risk.
- **Put Cloudflare Access in front of it** (Zero Trust → Access → Applications).
  A one-time email PIN takes two minutes to configure and means only you can
  reach the login page at all. Strongly recommended for a course project that
  does not need to be world-readable.
- **Do not expose the Proxmox web UI through the same tunnel.**

To change a password after deployment, edit `.env` and restart the backend —
no rebuild needed:

```bash
docker compose up -d backend
```

Note that this only affects accounts created *after* the change. Existing users
keep their old password until the nightly reset recreates them.

---

## Day-to-day operations

### Logs

```bash
docker compose logs -f backend
```

```bash
docker compose logs -f --tail=100
```

### Updating to a new version

```bash
git pull && docker compose up -d --build
```

The database volume survives rebuilds. Only wipe it deliberately (see below).

### Restarting everything

```bash
docker compose restart
```

### Backups

Proxmox-level backups are the simplest safety net: **Datacenter → Backup**, add
a scheduled job for the guest. That captures the whole thing, Docker volume
included.

For a database-only dump:

```bash
docker compose exec -T db sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --databases wolfcafe' > wolfcafe-backup.sql
```

Restore it with:

```bash
docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < wolfcafe-backup.sql
```

### The nightly reset

By design the database wipes and reseeds every night at midnight in
`DB_RESET_ZONE`. It clears orders, recipes, items, ingredients, the inventory,
saved tax rates, and **all user accounts**, then recreates `admin`, `staff`, and
`customer`.

Anything you want to keep will not survive the night. To turn it off:

```bash
# in .env
DB_RESET_ENABLED=false
```

```bash
docker compose up -d backend
```

To change the time, edit `DB_RESET_CRON` — it is a Spring six-field cron
(`second minute hour day-of-month month day-of-week`), so `0 0 4 * * *` is 4am.

### Starting over completely

```bash
docker compose down -v && docker compose up -d --build
```

`-v` deletes the database volume. Everything goes back to three seeded accounts
and no data.

---

## Troubleshooting

**`backend` never becomes healthy.**
Check `docker compose logs backend`. Usually the database is not reachable —
confirm `db` is healthy first. The backend waits for it, but a wrong
`DB_PASSWORD` will show as an access-denied loop.

**The site loads but every request fails with 502.**
nginx cannot reach the backend. Confirm `docker compose ps` shows `backend` as
healthy; nginx proxies to the service name `backend:8080` over the Docker
network.

**The site loads but login fails with a network error.**
Check the browser console. If requests are going somewhere other than
`/api/...`, `VITE_API_BASE_URL` was overridden at build time. Set it back to
`/api` in `.env` and rebuild the frontend.

**The tunnel shows the hostname as down.**
With the sidecar, the public hostname must point at `frontend:80`, not
`localhost`. `docker compose logs cloudflared` will say what it tried to reach.

**Docker will not start in an LXC.**
Nesting and keyctl are not enabled. See Step 1, Option B — or move to a VM.

### If the build is too heavy for the guest

On a small guest you can build the images on a machine with more RAM and ship
them over:

```bash
docker compose build && docker save csc326-2026-spring-tp-212-3-backend csc326-2026-spring-tp-212-3-frontend | gzip > wolfcafe-images.tar.gz
```

Copy that to the guest, then:

```bash
gunzip -c wolfcafe-images.tar.gz | docker load && docker compose up -d
```

---

## A note on what is verified here

The Docker stack itself is tested — it was built and run end to end, with all
three services healthy, login working through the nginx proxy, and the
`mysqldump` and loopback-binding commands above confirmed working.

The Proxmox guest creation and Cloudflare dashboard steps are standard
procedure and were not executed against a live Proxmox host, so treat the exact
menu names as a guide to the right place rather than a transcript.
