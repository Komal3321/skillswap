# SkillSwap

SkillSwap is a peer-to-peer skill exchange and learning marketplace. This repository currently contains a standard React frontend and Spring Boot backend skeleton.

## Project Structure

```text
skillswap/
  frontend/
    public/
    src/
      assets/
      components/
      pages/
      layouts/
      routes/
      services/
      hooks/
      context/
      store/
      utils/
      styles/
      App.jsx
      main.jsx
    package.json
    vite.config.js
    tailwind.config.js
    postcss.config.js
    Dockerfile
  backend/
    src/
      main/
        java/com/skillswap/
        resources/application.yml
      test/
    pom.xml
    Dockerfile
    mvnw
  docs/
  docker-compose.yml
  .env.example
  .gitignore
  README.md
```

## Technology Stack

- Frontend: React 19, JavaScript, Vite, Tailwind CSS, Redux Toolkit, React Router, React Query, Axios, Framer Motion.
- Backend: Java 21, Spring Boot 3, Maven, Spring Security, JWT placeholders, Spring Data JPA, Hibernate, MySQL 8, Redis.
- Infrastructure: Docker and Docker Compose.

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

The `frontend` and `backend` Dockerfiles are included for the application containers.

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend skeleton is configured with Vite, Tailwind CSS, React, Redux Toolkit, React Router, React Query, Axios, and Framer Motion. Pages, routes, stores, and UI features are intentionally not generated yet.

## Backend Setup

```bash
cd backend
./mvnw spring-boot:run
```

The backend skeleton includes Maven dependencies and `application.yml` configuration for MySQL, Redis, and JWT placeholders. Controllers, services, repositories, entities, and API implementations are intentionally not generated yet.

## Environment

Use `.env.example` as the source for local environment variables. Replace `JWT_SECRET` before using any real authentication flow.
