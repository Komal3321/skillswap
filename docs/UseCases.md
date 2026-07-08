# Use Cases

## UC-01: Register Account

**Primary Actor:** Guest

**Goal:** Create a SkillSwap account.

**Preconditions:** The guest does not already have an account using the same email.

**Main Flow:**

1. Guest opens the registration page.
2. Guest enters name, email, password, and required consent.
3. System validates the submitted information.
4. System creates an inactive or limited account.
5. System sends an email verification message.
6. Guest verifies email.
7. System activates the account.

**Alternative Flows:**

- If the email already exists, the system prompts the guest to log in or reset password.
- If verification expires, the user can request a new verification email.

**Postconditions:** The user has an active account.

## UC-02: Create Public Profile

**Primary Actor:** Registered User

**Goal:** Create a profile that can be discovered by other users.

**Preconditions:** User is authenticated.

**Main Flow:**

1. User opens profile settings.
2. User enters bio, photo, timezone, languages, and location preference.
3. User chooses discoverability settings.
4. System validates and saves the profile.
5. System updates profile completion status.

**Postconditions:** The user's profile is available according to selected visibility settings.

## UC-03: Add Skill Offered

**Primary Actor:** Mentor

**Goal:** Publish a skill that the user can teach.

**Preconditions:** User is authenticated and has a basic profile.

**Main Flow:**

1. User selects "Add skill offered."
2. User enters skill title, category, description, proficiency, teaching format, duration, exchange type, and optional price.
3. User adds supporting evidence such as links or credentials.
4. System validates the listing.
5. System publishes or saves the listing.

**Alternative Flows:**

- If the skill violates platform policy, the system blocks or flags it for review.
- If required fields are missing, the system shows validation errors.

**Postconditions:** The skill can appear in search and recommendations.

## UC-04: Add Skill Wanted

**Primary Actor:** Learner

**Goal:** Describe a skill the user wants to learn.

**Preconditions:** User is authenticated.

**Main Flow:**

1. User selects "Add skill wanted."
2. User enters category, target level, goals, preferred format, and availability.
3. System validates and saves the wanted skill.
4. System uses the entry for matching and recommendations.

**Postconditions:** The wanted skill contributes to user discovery and recommendations.

## UC-05: Search for a Skill

**Primary Actor:** Learner

**Goal:** Find mentors or swap partners for a desired skill.

**Preconditions:** Search index contains published skill listings.

**Main Flow:**

1. User enters a search query or selects a category.
2. User applies filters such as price, level, rating, language, format, or availability.
3. System returns ranked results.
4. User opens a profile or skill listing.

**Alternative Flows:**

- If no results are found, the system suggests related categories or allows saving the search.

**Postconditions:** User has viewed relevant skill listings or profiles.

## UC-06: Request a Skill Swap

**Primary Actor:** Skill Swapper

**Goal:** Request a reciprocal learning exchange.

**Preconditions:** Both users have active profiles. Requester has at least one offered skill.

**Main Flow:**

1. Requester opens another user's offered skill.
2. Requester selects a skill they can offer in return.
3. Requester proposes time windows and session format.
4. Requester sends a message with learning goals.
5. System creates a swap request.
6. Recipient receives notification.
7. Recipient accepts, declines, or proposes changes.

**Alternative Flows:**

- Recipient may message requester before deciding.
- Request may expire if no action is taken.

**Postconditions:** A swap request is pending, confirmed, declined, changed, or expired.

## UC-07: Book a Paid Session

**Primary Actor:** Learner

**Goal:** Reserve and pay for a mentor session.

**Preconditions:** Mentor offers a paid skill. Learner is authenticated.

**Main Flow:**

1. Learner selects a paid skill listing.
2. Learner chooses an available time slot.
3. System displays price, fees, cancellation policy, and session details.
4. Learner confirms booking and submits payment.
5. Payment processor authorizes or captures payment.
6. System creates a confirmed booking.
7. System notifies learner and mentor.

**Alternative Flows:**

- If payment fails, booking is not confirmed.
- If the slot becomes unavailable, the system asks the learner to choose another time.

**Postconditions:** A paid booking exists with payment status recorded.

## UC-08: Reschedule a Booking

**Primary Actor:** Learner or Mentor

**Goal:** Change the time of a confirmed session.

**Preconditions:** Booking is confirmed and policy allows rescheduling.

**Main Flow:**

1. User opens booking details.
2. User selects "Request reschedule."
3. User proposes one or more new time slots.
4. System notifies the other participant.
5. Other participant accepts one proposed time.
6. System updates the booking and sends confirmation.

**Alternative Flows:**

- Other participant declines the proposal.
- Reschedule window has passed, so the system blocks the request.

**Postconditions:** Booking is updated or remains unchanged.

## UC-09: Send Message

**Primary Actor:** Authenticated User

**Goal:** Communicate with another user about a valid marketplace interaction.

**Preconditions:** Users have an active request, booking, or allowed contact relationship.

**Main Flow:**

1. User opens conversation.
2. User writes a message.
3. System validates message content.
4. System stores and delivers the message.
5. Recipient receives in-app and configured external notification.

**Alternative Flows:**

- If recipient blocked sender, message is rejected.
- If message is reported or flagged, moderation workflow may start.

**Postconditions:** Message is available in conversation history.

## UC-10: Complete Session and Review

**Primary Actor:** Learner, Mentor, or Swap Partner

**Goal:** Mark a session complete and submit feedback.

**Preconditions:** Booking time has passed and booking was not canceled.

**Main Flow:**

1. System marks booking eligible for completion.
2. Participants confirm completion or system completes after configured time.
3. System prompts participants to review each other.
4. User submits rating and feedback.
5. System publishes eligible review content.
6. System recalculates reputation metrics.

**Alternative Flows:**

- User reports a problem instead of reviewing.
- Review content is flagged for moderation.

**Postconditions:** Session contributes to ratings, history, and reputation.

## UC-11: Report User or Content

**Primary Actor:** User

**Goal:** Alert the platform to unsafe or inappropriate behavior.

**Preconditions:** User is authenticated.

**Main Flow:**

1. User selects report action on profile, skill, message, review, or booking.
2. User chooses reason and provides details.
3. System records report and evidence.
4. System places report in moderation queue.
5. Administrator reviews and takes action.

**Postconditions:** Report is tracked with resolution status.

## UC-12: Moderate Report

**Primary Actor:** Administrator

**Goal:** Review and resolve reported content or behavior.

**Preconditions:** Administrator has required permission.

**Main Flow:**

1. Administrator opens moderation queue.
2. Administrator reviews report, related user history, and evidence.
3. Administrator chooses an action.
4. System applies action and records audit event.
5. System notifies affected users when appropriate.

**Postconditions:** Report is resolved or escalated.
