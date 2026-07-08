# Folder Structure

## Purpose

This document describes the intended project organization for SkillSwap. It is a planning document only and does not require the folders to exist during the documentation phase.

The structure should keep frontend, backend, shared contracts, infrastructure, and documentation clearly separated while allowing the project to grow into a production system.

## Recommended Repository Layout

```text
skillswap/
  docs/
    SRS.md
    FunctionalRequirements.md
    NonFunctionalRequirements.md
    UserStories.md
    UseCases.md
    SystemArchitecture.md
    DatabaseDesign.md
    FolderStructure.md
    DevelopmentRoadmap.md
  apps/
    web/
    api/
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
  .github/
  README.md
```

## Directory Responsibilities

## docs

Contains product, architecture, requirements, and delivery documentation.

Expected contents:

- Requirements documents.
- Architecture decisions.
- Database design.
- API design notes.
- Security and privacy documentation.
- Runbooks and operational guides.

## apps/web

Contains the primary user-facing responsive web application.

Typical responsibilities:

- Public landing and discovery pages.
- Authenticated user dashboard.
- Profile management.
- Skill search and listings.
- Booking workflows.
- Messaging interface.
- Reviews and notifications.

## apps/api

Contains backend API application code.

Typical responsibilities:

- Authentication and authorization.
- Domain APIs for users, skills, bookings, messaging, payments, reviews, reports, and notifications.
- Validation and business rules.
- Integration with database, cache, queue, search, email, and payment providers.

## apps/worker

Contains background job processing.

Typical responsibilities:

- Transactional email delivery.
- Booking reminders.
- Search indexing.
- Payment webhook follow-up.
- Analytics aggregation.
- Scheduled cleanup jobs.

## apps/admin

Contains internal administration interface if separated from the main web app.

Typical responsibilities:

- User and content moderation.
- Report review.
- Category management.
- Dispute support.
- Operational dashboards.
- Staff role management.

## packages/shared

Contains shared domain types and constants used across applications.

Typical contents:

- Shared enums.
- API contracts.
- Common domain constants.
- Shared utility functions with no platform-specific dependencies.

## packages/config

Contains shared configuration conventions.

Typical contents:

- Lint configuration.
- Formatting configuration.
- Type checking configuration.
- Test configuration.
- Environment variable schemas.

## packages/ui

Contains reusable UI components if the project uses a shared component library.

Typical contents:

- Buttons, inputs, dialogs, menus, tabs, cards, and layout primitives.
- Design tokens.
- Accessibility helpers.
- Storybook or component documentation.

## packages/validation

Contains shared validation schemas for API and frontend forms.

Typical contents:

- User profile validation.
- Skill listing validation.
- Booking request validation.
- Review validation.
- Report validation.

## packages/testing

Contains shared testing utilities.

Typical contents:

- Test factories.
- Mock server helpers.
- Authentication helpers.
- Database cleanup helpers.
- Fixture builders.

## infrastructure

Contains deployment, environment, and operations definitions.

Typical contents:

- Infrastructure as code.
- Environment configuration templates.
- Database migration support.
- Monitoring and alerting configuration.
- Deployment scripts.

## infrastructure/database

Contains database-specific assets.

Typical contents:

- Migrations.
- Seed data.
- Schema snapshots.
- Data backfill scripts.

## tests

Contains cross-application tests.

Typical contents:

- End-to-end tests.
- Load tests.
- Security tests.
- Contract tests.

## tools

Contains developer tooling and one-off scripts.

Typical contents:

- Code generation scripts.
- Local setup helpers.
- Data import utilities.
- Maintenance scripts.

## .github

Contains GitHub-specific automation.

Typical contents:

- Pull request templates.
- Issue templates.
- CI workflows.
- Release workflows.

## Naming Guidelines

- Use lowercase directory names.
- Use feature-oriented folders inside application domains.
- Avoid mixing frontend-only, backend-only, and shared code in the same folder.
- Keep generated files clearly marked and excluded from manual editing.
- Keep test files near the code they verify for unit and integration tests, with cross-application tests in top-level `tests`.

## Backend Module Example

```text
apps/api/src/
  modules/
    auth/
    users/
    profiles/
    skills/
    discovery/
    bookings/
    messaging/
    payments/
    reviews/
    moderation/
    notifications/
    analytics/
  common/
    errors/
    middleware/
    security/
    database/
    jobs/
    logging/
```

## Frontend Feature Example

```text
apps/web/src/
  app/
  features/
    auth/
    onboarding/
    profile/
    skills/
    search/
    bookings/
    messages/
    reviews/
    notifications/
    settings/
  components/
  hooks/
  services/
  styles/
  tests/
```

## Documentation Growth Plan

Future documentation should include:

- API specification.
- Authentication and authorization model.
- Payment workflow specification.
- Moderation policy.
- Privacy and retention policy.
- Deployment runbook.
- Incident response guide.
- Architecture decision records.
