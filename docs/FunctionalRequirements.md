# Functional Requirements

## 1. User Account Management

### 1.1 Registration

- The system shall allow users to create an account using email and password.
- The system shall support email verification before enabling full account functionality.
- The system shall prevent duplicate accounts using the same verified email address.
- The system shall allow users to accept terms of service and privacy policy during registration.

### 1.2 Authentication

- The system shall allow registered users to log in securely.
- The system shall support password reset through verified email.
- The system shall invalidate sessions after logout.
- The system shall support secure session refresh without requiring frequent manual login.

### 1.3 Profile Management

- The system shall allow users to create and edit a public profile.
- The profile shall include display name, profile photo, bio, location preference, languages, timezone, and availability summary.
- The system shall allow users to choose whether their profile is discoverable.
- The system shall show profile completion progress to encourage better marketplace quality.

## 2. Skill Management

### 2.1 Skills Offered

- The system shall allow users to add skills they can teach.
- Each offered skill shall include title, category, description, proficiency level, teaching format, session duration, exchange type, and optional price.
- The system shall allow users to pause, edit, or remove offered skills.
- The system shall support evidence such as portfolio links, credentials, examples, or endorsements.

### 2.2 Skills Wanted

- The system shall allow users to add skills they want to learn.
- Each wanted skill shall include category, target level, learning goals, preferred format, and availability.
- The system shall use wanted skills to improve recommendations and matching.

### 2.3 Categories and Tags

- The system shall provide skill categories and searchable tags.
- Administrators shall be able to manage categories, aliases, and restricted tags.
- The system shall prevent abusive, misleading, or prohibited skill tags.

## 3. Discovery and Search

- The system shall allow users to search for mentors, learners, and skills.
- The system shall support filters for category, level, availability, format, language, location, rating, price, and exchange type.
- The system shall show relevant profile and skill cards in search results.
- The system shall rank results using relevance, activity, reputation, and availability.
- The system shall support saved searches in a future release.

## 4. Matching

- The system shall recommend potential swaps when one user's offered skill matches another user's wanted skill.
- The system shall indicate why a match is suggested.
- The system shall support reciprocal matching where both users can teach each other.
- The system shall allow users to dismiss, save, or contact recommended matches.

## 5. Requests and Bookings

### 5.1 Swap Requests

- The system shall allow users to send a skill swap request.
- The request shall include requested skill, offered skill, preferred time windows, message, format, and expected session length.
- The recipient shall be able to accept, decline, propose changes, or message before deciding.

### 5.2 Paid Bookings

- The system shall allow mentors to offer paid sessions.
- The system shall show price, duration, cancellation policy, and platform fee before checkout.
- The system shall create a booking only after successful payment authorization or configured payment confirmation.
- The system shall support refund and cancellation workflows according to policy.

### 5.3 Booking Lifecycle

- Bookings shall support statuses such as requested, pending payment, confirmed, reschedule proposed, completed, canceled, disputed, and expired.
- The system shall notify both parties when booking status changes.
- The system shall prevent double booking for confirmed availability slots.

## 6. Availability and Scheduling

- The system shall allow users to define recurring availability.
- The system shall allow users to block unavailable dates.
- The system shall display available time slots in the viewer's timezone.
- The system shall support reschedule requests.
- The system shall send reminders before confirmed sessions.

## 7. Messaging

- The system shall provide in-app direct messaging between users who have an active request, booking, or accepted contact interaction.
- The system shall support text messages and system-generated booking messages.
- The system shall allow users to report messages.
- The system shall moderate or flag messages that include prohibited content where feasible.
- The system shall restrict sharing of sensitive personal information based on platform policy if required.

## 8. Reviews and Reputation

- The system shall allow users to review each other after a completed session.
- Reviews shall include rating, written feedback, skill-specific feedback, and optional private feedback to the platform.
- The system shall calculate mentor rating and completed session counts.
- The system shall prevent users from reviewing sessions they did not participate in.
- The system shall allow administrators to hide or remove abusive reviews.

## 9. Notifications

- The system shall send in-app notifications for requests, booking changes, messages, reminders, reviews, reports, and administrative actions.
- The system shall send transactional emails for important events.
- Users shall be able to manage notification preferences.
- Critical safety and account notifications shall not be fully disabled.

## 10. Payments

- The system shall integrate with a payment processor for paid sessions.
- The system shall support payment authorization, capture, refund, and payout workflows.
- The system shall record payment status separately from booking status.
- The system shall never store raw payment card details.
- The system shall provide users with receipts for paid transactions.

## 11. Safety and Moderation

- The system shall allow users to block other users.
- The system shall allow users to report profiles, skills, messages, bookings, and reviews.
- Administrators shall be able to review reports and take action.
- Administrative actions shall include warning, content removal, account suspension, account ban, refund recommendation, and dispute escalation.
- The system shall keep an audit trail of moderation decisions.

## 12. Administration

- Administrators shall be able to view user accounts, public profiles, reports, disputes, bookings, and payment summaries.
- Administrators shall be able to manage categories, featured skills, restricted content, and platform settings.
- Administrators shall have role-based permissions.
- The system shall log privileged administrative actions.

## 13. Analytics

- The system shall track key marketplace metrics such as registrations, completed profiles, searches, requests, accepted bookings, completed sessions, cancellations, reviews, and reports.
- Administrators shall have access to operational dashboards.
- User-facing analytics shall show mentors basic performance metrics such as views, requests, acceptance rate, and rating.

## 14. Content Management

- The system shall provide static informational pages such as help, safety guidelines, terms, privacy policy, and community standards.
- Administrators shall be able to update selected help and policy content through an approved publishing process.

## 15. Accessibility

- The system shall support keyboard navigation for primary workflows.
- Form fields shall include accessible labels and validation messages.
- Dynamic updates shall be announced appropriately where required.
