# WolfCafe 

## Deployment with Docker

The whole stack runs from one compose file: MySQL, the Spring Boot API, and
nginx serving the built React app.

```bash
cp .env.example .env      # then edit the passwords and JWT secret
docker compose up -d --build
```

The site is then served on `http://<host>:8080` (change with `WEB_PORT`).

> **Deploying to a server?** [DEPLOYMENT.md](DEPLOYMENT.md) is a step-by-step
> guide for Proxmox and Cloudflare Tunnel, covering guest sizing, secrets,
> backups, and troubleshooting.

### Why it works on any host

The frontend never hardcodes a hostname. It calls the **relative** path `/api`,
and nginx reverse-proxies that to the backend container. Every request is
therefore same-origin, so the exact same image works at `localhost`, at a LAN IP
such as `192.168.1.50:8080`, and behind a Cloudflare Tunnel hostname -- with no
rebuild and no CORS configuration.

On the backend side every setting is read from an environment variable with a
local-development fallback (see `wolf-cafe-backend/src/main/resources/application.properties`),
so database credentials, the JWT secret, and the seeded account passwords are
all supplied at run time rather than baked into the image.

### Cloudflare Tunnel

Point the tunnel's public hostname at the frontend. Either run `cloudflared` on
the Proxmox host against `http://localhost:8080`, or run it as a sidecar in the
stack:

```bash
# add TUNNEL_TOKEN to .env first, and route the hostname to http://frontend:80
docker compose --profile tunnel up -d
```

The backend honours `X-Forwarded-*` headers (`server.forward-headers-strategy`),
so it sees the tunnel's real scheme and hostname.

### Configuration reference

All variables live in `.env`; see `.env.example` for the full annotated list.
The ones worth knowing:

| Variable | Default | Purpose |
| --- | --- | --- |
| `WEB_PORT` | `8080` | Host port the site is served on |
| `DB_PASSWORD` | *required* | MySQL root password |
| `JWT_SECRET` | *required* | Signing key; generate with `openssl rand -hex 32` |
| `DEFAULT_PASSWORD` | *required* | Password for the seeded `admin`, `staff`, and `customer` accounts |
| `DB_RESET_CRON` | `0 0 0 * * *` | When the nightly reset runs (Spring 6-field cron) |
| `DB_RESET_ZONE` | `America/New_York` | Time zone for that schedule |
| `DB_RESET_ENABLED` | `true` | Set `false` to turn the reset off |
| `PROFANITY_FILTER_ENABLED` | `true` | Set `false` to disable text screening |
| `VITE_API_BASE_URL` | `/api` | Only change if the API is on a separate origin |

### Useful commands

```bash
docker compose logs -f backend
```

```bash
docker compose up -d --build backend
```

```bash
docker compose down          # add -v to also drop the database volume
```

## Nightly database reset

`config.DatabaseResetScheduler` wipes and reseeds the database every night at
midnight (`America/New_York` by default). It clears orders, recipes, items,
ingredients, the inventory, saved tax rates, and all user accounts, then
recreates the default roles and the `admin`, `staff`, and `customer` accounts
using `DEFAULT_PASSWORD` from the environment.

The schedule is `DB_RESET_CRON` / `DB_RESET_ZONE`, and `DB_RESET_ENABLED=false`
disables it entirely. The test configuration disables it so tests never wipe a
database.

## Profanity filter

`service.ProfanityFilterService` screens every free-text field a user can set:
account name, username, and email at registration and in admin user management;
drink and item names; item descriptions; and ingredient names in both recipes
and the inventory. Rejected input returns `400` with a message naming the field,
which the UI displays inline.

The word lists live in `wolf-cafe-backend/src/main/resources/profanity/`:

  * `terms.txt` -- blocked terms, matched as whole words.
  * `strict-terms.txt` -- the subset also matched as a substring, which catches
    padded obfuscation such as `s.h.i.t` and `f u c k`.
  * `allowed-words.txt` -- benign words containing a strict term, removed before
    the substring scan.

Matching lowercases the text, folds leetspeak (`sh1t`, `@ss`), and tolerates
repeated letters (`shiiiit`). Whole-word matching for the ambiguous terms is
what keeps ordinary names like "Pumpkin Spice Latte", "Lemongrass Tea", and
"Grape Juice" from being rejected. To add a term, put it in `terms.txt`; only
add it to `strict-terms.txt` if it never appears inside an ordinary word.

## Install Lombok
Lombok is a library that lets us use annotations to automatically generate getters, setters, and constructors.  For Lombok to work in Eclipse (and other IDEs like IntelliJ or VS Code), you need to set up Lombok with the IDE in addition to including in the pom.xml file.

Follow the [instructions for setting up Lombok in Eclipse](https://projectlombok.org/setup/eclipse) (you may need to specify the location of your Eclipse installation).  Make sure you download the latest version of Lombok from [Maven Repository](https://mvnrepository.com/artifact/org.projectlombok/lombok) as a jar file. If you have a `development` directory for your Eclipse installations, it's helpful to add the Lombok jar there.

## Configuration

For the Docker deployment you do not edit these files -- every setting is read
from an environment variable (see the Deployment section above and
`.env.example`). The instructions below apply to running the app directly from
an IDE.

Update `application.properties` in `src/main/resources/` and `src/test/resources/`.

  * Set `spring.datasource.password` to your local MySQL password`
  * Set `app.jwt-secret` as described below.
  * Set `app.default-user-password` to a plain text string; it is the password for all three seeded accounts.
  
### Set `app.jwt-secret`

We will create a secret key that will be used for JWT authentication.  Think of a secret key phrase.  You'll want to encrypt it using SHA256 encryption.  You can use a tool like:  https://emn178.github.io/online-tools/sha256.html to generate the encrypted text.  Copy that into your `application.properties` file.

## Setup
The rest of the setup for WolfCafe is the same as for [CoffeeMaker](https://courses.csc.ncsu.edu/csc326/onboarding/setup/).

### Running the frontend locally

```bash
cd wolf-cafe-frontend && npm install && npm run dev
```

The dev server proxies `/api` to `http://localhost:8080`, so the frontend code
uses the same relative paths in development as it does in production. Point it
somewhere else with `DEV_API_PROXY_TARGET` in `wolf-cafe-frontend/.env`.


## User Roles

User roles are defined and initialized in `config.Roles`.  The `ADMIN` role is a constant.  All other roles are listed in the `UserRoles` enumeration. You can add new roles by adding the role name to the enumeration.

### Initializing the Roles/Admin user in the DB

`config.SetupDataLoader` initializes the DB with roles and creates a default user with the `ADMIN` role.  This class is automatically run when the application starts.  

Three accounts are seeded, one per role:

| Username | Email | Role |
| --- | --- | --- |
| `admin` | admin@wolfcafe.edu | ADMIN |
| `staff` | staff@wolfcafe.edu | STAFF |
| `customer` | customer@wolfcafe.edu | CUSTOMER |

They all share the password from `app.default-user-password` (`DEFAULT_PASSWORD` in Docker), which is read from the configuration and then hashed with the password encoder. Because the usernames are well known, change that value before putting the site on a public hostname.  

## Testing User Authentication in Postman

The following provides examples of how to work with user authentication in Postman.

### Create a New User

Endpoint: `POST http://localhost:8080/api/auth/register`

Body:

```
{
    "name": "Sarah Heckman",
    "username": "sheckman",
    "email": "sheckman@ncsu.edu",
    "password": "sarah"
}
```

Response: 201 Created

```
User registered successfully.
```

### Login with User

Endpoint: `POST http://localhost:8080/api/auth/login`

Body: 

```
{
    "usernameOrEmail": "sheckman",
    "password": "sarah"
}
```

Response: 200 OK

```
{
    "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzaGVja21hbiIsImlhdCI6MTcyOTEyNjg1MiwiZXhwIjoxNzI5NzMxNjUyfQ.WiPROZAMhNbiB8H3fhNJdiC-XX5RJEcHXzmGPEH7aMEFvsjbsvk2m1ZcAKi-lTdt",
    "tokenType": "Bearer",
    "role": "ROLE_CUSTOMER"
}
```

Note the accessToken will vary with each login.  You'll want to save this for testing endpoints that require authentication.

### Get Items

Endpoint: `GET http://localhost:8080/api/items`

Roles: STAFF, CUSTOMER

Authorization:
  * Bearer
  * Token - copy from the response of an authenticated User
  
Response: 200 OK

```
JSON list of items
```
