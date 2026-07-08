# Database Design

## Design Goals

- Preserve transactional integrity for accounts, bookings, payments, reviews, and moderation.
- Support efficient search, filtering, and profile discovery.
- Keep user-generated content auditable and moderatable.
- Allow a user to be both learner and mentor.
- Support future mobile clients, analytics, and international expansion.

## Recommended Database Model

A relational database is recommended for the core system because SkillSwap has transactional workflows, relational marketplace data, and consistency requirements around bookings and payments. A separate search index may be introduced for full-text discovery.

## Core Entities

## users

Stores account-level identity and status.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| email | String | Unique, normalized |
| password_hash | String | Nullable if external auth is used |
| email_verified_at | Timestamp | Nullable |
| status | Enum | active, limited, suspended, banned, deleted |
| role | Enum | user, support, admin, super_admin |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |
| last_login_at | Timestamp | Nullable |

## profiles

Stores public and preference information.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| display_name | String | Required |
| bio | Text | Nullable |
| photo_url | String | Nullable |
| timezone | String | Required |
| location_text | String | Nullable |
| location_mode | Enum | online, local, hybrid |
| discoverable | Boolean | Default true |
| languages | JSON or related table | Normalized later if needed |
| profile_completion_score | Integer | 0-100 |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## skill_categories

Stores controlled taxonomy.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| parent_id | UUID | Nullable self-reference |
| name | String | Unique within parent |
| slug | String | Unique |
| status | Enum | active, hidden, archived |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## skills_offered

Stores skills a user can teach.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| category_id | UUID | Foreign key to skill_categories |
| title | String | Required |
| slug | String | Unique per user or globally |
| description | Text | Required |
| proficiency_level | Enum | beginner, intermediate, advanced, expert |
| teaching_format | Enum | online, in_person, hybrid |
| exchange_type | Enum | swap, paid, swap_or_paid |
| price_amount | Decimal | Nullable |
| price_currency | String | Nullable |
| default_duration_minutes | Integer | Required |
| status | Enum | draft, active, paused, flagged, removed |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## skills_wanted

Stores skills a user wants to learn.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| category_id | UUID | Foreign key to skill_categories |
| title | String | Required |
| goals | Text | Nullable |
| target_level | Enum | beginner, intermediate, advanced |
| preferred_format | Enum | online, in_person, hybrid |
| status | Enum | active, paused, archived |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## skill_tags

Stores reusable tags.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| name | String | Unique |
| slug | String | Unique |
| status | Enum | active, restricted, archived |

## skill_taggables

Associates tags with offered or wanted skills.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| tag_id | UUID | Foreign key to skill_tags |
| entity_type | Enum | offered, wanted |
| entity_id | UUID | ID of associated skill |

## availability_rules

Stores recurring availability.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| day_of_week | Integer | 0-6 |
| start_time_local | Time | Required |
| end_time_local | Time | Required |
| timezone | String | Required |
| active | Boolean | Required |

## availability_blocks

Stores exceptions and blocked dates.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| starts_at | Timestamp | UTC |
| ends_at | Timestamp | UTC |
| reason | String | Nullable |

## booking_requests

Stores requests before final confirmation.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| requester_id | UUID | Foreign key to users |
| recipient_id | UUID | Foreign key to users |
| requested_skill_id | UUID | Foreign key to skills_offered |
| offered_skill_id | UUID | Nullable, for swaps |
| type | Enum | swap, paid |
| message | Text | Nullable |
| status | Enum | pending, accepted, declined, countered, expired, canceled |
| expires_at | Timestamp | Nullable |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## bookings

Stores confirmed or lifecycle-managed sessions.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| request_id | UUID | Nullable foreign key to booking_requests |
| learner_id | UUID | Foreign key to users |
| mentor_id | UUID | Foreign key to users |
| skill_offered_id | UUID | Foreign key to skills_offered |
| type | Enum | swap, paid |
| status | Enum | pending_payment, confirmed, completed, canceled, disputed, no_show |
| starts_at | Timestamp | UTC |
| ends_at | Timestamp | UTC |
| meeting_mode | Enum | online, in_person, hybrid |
| meeting_details | Text | Nullable, access-controlled |
| cancellation_reason | Text | Nullable |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## payments

Stores normalized payment records.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| booking_id | UUID | Foreign key to bookings |
| payer_id | UUID | Foreign key to users |
| payee_id | UUID | Foreign key to users |
| provider | String | Required |
| provider_payment_id | String | Unique |
| amount | Decimal | Required |
| currency | String | Required |
| platform_fee | Decimal | Required |
| status | Enum | requires_payment, authorized, captured, refunded, failed, disputed |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## conversations

Stores message threads.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| booking_id | UUID | Nullable foreign key to bookings |
| request_id | UUID | Nullable foreign key to booking_requests |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## conversation_participants

Stores conversation membership.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| conversation_id | UUID | Foreign key to conversations |
| user_id | UUID | Foreign key to users |
| last_read_at | Timestamp | Nullable |

## messages

Stores user and system messages.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| conversation_id | UUID | Foreign key to conversations |
| sender_id | UUID | Nullable for system messages |
| type | Enum | user, system |
| body | Text | Required |
| status | Enum | sent, hidden, removed |
| created_at | Timestamp | Required |

## reviews

Stores post-session feedback.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| booking_id | UUID | Foreign key to bookings |
| reviewer_id | UUID | Foreign key to users |
| reviewee_id | UUID | Foreign key to users |
| rating | Integer | 1-5 |
| public_comment | Text | Nullable |
| private_comment | Text | Nullable |
| status | Enum | published, hidden, removed, flagged |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## reports

Stores user reports for moderation.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| reporter_id | UUID | Foreign key to users |
| reported_user_id | UUID | Nullable foreign key to users |
| entity_type | Enum | profile, skill, message, booking, review |
| entity_id | UUID | Required |
| reason | Enum | Required |
| details | Text | Nullable |
| status | Enum | open, reviewing, resolved, dismissed, escalated |
| created_at | Timestamp | Required |
| updated_at | Timestamp | Required |

## blocks

Stores user blocking relationships.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| blocker_id | UUID | Foreign key to users |
| blocked_id | UUID | Foreign key to users |
| created_at | Timestamp | Required |

## notifications

Stores in-app notifications.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| type | String | Required |
| title | String | Required |
| body | Text | Nullable |
| entity_type | String | Nullable |
| entity_id | UUID | Nullable |
| read_at | Timestamp | Nullable |
| created_at | Timestamp | Required |

## audit_logs

Stores privileged and security-relevant events.

| Field | Type | Notes |
| --- | --- | --- |
| id | UUID | Primary key |
| actor_id | UUID | Nullable foreign key to users |
| action | String | Required |
| entity_type | String | Required |
| entity_id | UUID | Nullable |
| metadata | JSON | Nullable |
| created_at | Timestamp | Required |

## Relationships

- A user has one profile.
- A user has many offered skills and wanted skills.
- A skill category has many offered and wanted skills.
- A booking request may become one booking.
- A booking belongs to a learner, mentor, and offered skill.
- A paid booking has one or more payment records depending on retries and refunds.
- A conversation can be linked to a request or booking.
- A booking can have up to two participant reviews.
- Reports can reference multiple entity types through a polymorphic relationship.
- Blocks prevent messaging and selected discovery interactions.

## Indexing Strategy

- Unique index on `users.email`.
- Index on `profiles.discoverable`.
- Index on `skills_offered.status`, `category_id`, `exchange_type`, and `teaching_format`.
- Full-text index on skill title, description, profile display name, and bio where supported.
- Composite indexes on `bookings.mentor_id + starts_at` and `bookings.learner_id + starts_at`.
- Index on `messages.conversation_id + created_at`.
- Index on `notifications.user_id + read_at + created_at`.
- Index on `reports.status + created_at`.
- Unique index on `blocks.blocker_id + blocked_id`.

## Data Integrity Rules

- A user cannot review the same booking more than once.
- A user cannot book a confirmed slot that overlaps with another confirmed booking for the same participant.
- Paid bookings must have a valid payment state before confirmation unless explicitly configured otherwise.
- Removed or banned users should not appear in public search.
- Booking state transitions must follow an approved state machine.
- Messages must only be created by valid conversation participants.

## Retention Considerations

- Deleted accounts may be anonymized instead of hard deleted when transaction, safety, or audit records must remain.
- Payment records should be retained according to financial requirements.
- Moderation evidence should have defined retention periods.
- Logs should avoid storing sensitive message content unless explicitly required for safety review.

## Future Extensions

- Calendar integration tables.
- Group sessions and class cohorts.
- Saved searches and watch alerts.
- Endorsements independent of bookings.
- Credential verification records.
- Recommendation event tables.
- Region-specific tax and payout records.
