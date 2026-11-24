# ShareSphere

ShareSphere is a community resource-sharing platform. The initial release focuses on **BorrowCircle**; SpaceShare and WasteMatch are future service modules.

## Project structure

```text
apps/web/                 React + TypeScript + Tailwind frontend
backend/gateway/          Spring Cloud Gateway and JWT validation
backend/*-service/        Independently buildable Spring Boot services
infra/postgres/           Local database initialization
docker-compose.yml        Full local stack
```

The backend modules are Authentication & Users, Communities, BorrowCircle, Bookings & Transactions, Payments, Chat, Notifications, Reviews, and Admin. Each domain service has its own PostgreSQL database; RabbitMQ distributes booking events to the notifications service.

## Prerequisites

- Java 25 and Maven 3.9+
- Node.js 18+ and npm
- Docker Desktop with Docker Compose (optional)

## Full Docker setup

1. Copy `.env.example` to `.env` and change the local secrets if desired. Set `ADMIN_EMAILS` to a comma-separated allowlist to grant platform administrator access to accounts created with those addresses.
2. From the repository root, run:

   ```powershell
   docker compose up --build
   ```

3. Open the web app at [http://localhost:3000](http://localhost:3000). The API gateway is on port `8080`; RabbitMQ management is on port `15672`.

PostgreSQL creates nine service databases on first initialization. To recreate them after changing initialization settings, remove the local `postgres-data` volume with `docker compose down -v` (this deletes local database data).

## Native development setup

Start only the infrastructure containers first:

```powershell
docker compose up -d postgres rabbitmq
```

In separate terminals, run the services from `backend` (each service uses its own default database name and port):

```powershell
mvn -pl auth-service spring-boot:run
mvn -pl community-service spring-boot:run
mvn -pl borrowcircle-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl payments-service spring-boot:run
mvn -pl chat-service spring-boot:run
mvn -pl notifications-service spring-boot:run
mvn -pl reviews-service spring-boot:run
mvn -pl admin-service spring-boot:run
mvn -pl gateway spring-boot:run
```

For local registration and admin access, set `JWT_SECRET` consistently for auth and gateway. Set `INTERNAL_SERVICE_TOKEN` consistently for auth, community, BorrowCircle, bookings, payments, chat, reviews, and admin. Set `ADMIN_EMAILS` in the auth service if you need an admin account. RabbitMQ defaults to `localhost:5672` with user `sharesphere` and password `sharesphere_local`.

In another terminal, run the frontend:

```powershell
cd apps/web
npm install
npm run dev
```

The native frontend uses `http://localhost:8080` as its API base by default. Override with `VITE_API_BASE_URL` when needed.

## Included flows

- Email/password registration and sign-in, BCrypt password hashing, JWT access tokens, profiles, and an optional UPI ID.
- Community creation, invitation links, invite acceptance, and community membership checks before publishing or requesting community-only listings.
- BorrowCircle listing creation, editing, archiving, community-only/public visibility, INR paid/free/negotiable terms, availability windows, category and text discovery, and nearby public discovery with distance filtering.
- Booking requests, reasoned counteroffers, explicit offer acceptance, provider approval/decline, date-conflict checks, handover/return tracking, cancellation, and dispute recording.
- UPI intent generation from the provider's profile VPA. A signed HMAC webhook updates the paid booking state. Configure a provider webhook adapter and secret before processing real payments.
- RabbitMQ booking events and a persisted notification inbox; participant-checked chat history with live server-sent updates; completed-exchange reviews; and an administrator dispute queue.
- OpenAPI/Swagger UI for the MVC services at each service's `/swagger-ui.html` path when running natively.

## UPI provider integration

The payment service creates a UPI URI and validates a provider-neutral HMAC webhook envelope (`paymentReference`, `bookingId`, `transactionRef`, `amountInr`, and `status`). Set `UPI_WEBHOOK_SECRET` before accepting callbacks. A production UPI provider adapter, provider-specific signature verification, payment refunds, and settlement handling still need to be configured for the selected processor.

## Local security notes

The defaults in Compose and `.env.example` are for local development. Change JWT, internal-service, database, and webhook secrets before using a shared or deployed environment. Internal service endpoints are blocked at the gateway and require the shared internal token for service-to-service calls.

## Spring version alignment

The backend targets Spring Boot 3.5.x with Spring Cloud 2025.0.x. The official compatibility table pairs those release trains, and Boot 3.5 supports Java 25: [Spring Boot requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Spring Cloud compatibility](https://spring.io/projects/spring-cloud/).