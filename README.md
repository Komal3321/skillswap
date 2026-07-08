# SkillSwap

SkillSwap is a peer-to-peer skill exchange and learning marketplace. This repository currently contains the project skeleton and configuration for the frontend, backend, infrastructure, shared packages, and cross-application tests.

## Project Structure

```text
skillswap/
  docs/
  apps/
    web/
      public/
      src/
        app/
        features/
        components/
        hooks/
        services/
        styles/
        tests/
      Dockerfile
      package.json
    api/
      src/
        main/
          java/com/skillswap/api/
            common/
            modules/
          resources/application.yml
        test/java/com/skillswap/api/
      Dockerfile
      pom.xml
    worker/
    admin/
  packages/
    shared/
    config/
    ui/
    validation/
    testing/
  infrastructure/
    environments/
    scripts/
    database/
    observability/
  tests/
    e2e/
    load/
    security/
  tools/
  .github/workflows/
  docker-compose.yml
  .env.example
```

## Technology Stack

- Frontend: React 19, JavaScript, Vite, Tailwind CSS, Redux Toolkit, React Router, React Query, Axios, Framer Motion.
- Backend: Java 21, Spring Boot 3, Maven, Spring Security, JWT placeholders, Spring Data JPA, Hibernate, MySQL 8, Redis.
- Infrastructure: Docker, Docker Compose, GitHub Actions folder structure.

## Local Setup

1. Copy the environment template:

```bash
cp .env.example .env
```

2. Start MySQL and Redis for local development:

```bash
docker compose up mysql redis
```

3. Frontend default port:

```text
http://localhost:5173
```

4. Backend default port:

```text
http://localhost:8080
```

The `web` and `api` Dockerfiles are included for the application containers. They will become buildable after the frontend entrypoint and Spring Boot application class are added in the implementation phase.

## Frontend Setup

```bash
cd apps/web
npm install
npm run dev
```

The frontend skeleton is configured through `package.json` dependencies and scripts. Application entry files, routes, pages, stores, and UI implementation are intentionally not generated yet.

## Backend Setup

```bash
cd apps/api
mvn spring-boot:run
```

The backend skeleton includes Maven dependencies and `application.yml` configuration for MySQL, Redis, and JWT placeholders. Controllers, services, repositories, entities, and API implementations are intentionally not generated yet.

## Environment

Use `.env.example` as the source for local environment variables. Replace `JWT_SECRET` before using any real authentication flow.
