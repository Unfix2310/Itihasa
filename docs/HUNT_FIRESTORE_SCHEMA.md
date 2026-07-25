# Hunt Firestore Data & Security Architecture

The app reads published hunts from these paths:

```text
heritage_sites/{siteId}/hunts/{huntId}
heritage_sites/{siteId}/hunts/{huntId}/challenges/{challengeId}
```

The secret answer keys are stored in a deny-all server subcollection:

```text
heritage_sites/{siteId}/hunts/{huntId}/answers/{challengeId}
```

## Security Rules (`firestore.rules`)

1. **Public Read-Only Data:** `heritage_sites`, `hunts`, and `challenges` are read-only for authenticated users.
2. **Deny-All Secret Answers:** `heritage_sites/{siteId}/hunts/{huntId}/answers/{challengeId}` has `allow read, write: if false;`. Client SDKs cannot query or read answer keys.
3. **User Ownership:** `/users/{email}` and `/users/{email}/hunt_progress/{progressId}` can only be accessed by the owning user (`request.auth.token.email == email`).
4. **Point Protection:** `earnedPoints` and progress advancement are computed and written server-side by Firebase Cloud Functions.

## Server-Side Answer Verification (`verifyChallengeAnswer`)

Answers are validated server-side by the `verifyChallengeAnswer` Callable Cloud Function:

- **App Check Enforcement:** Mandatory Firebase App Check verification (`context.app`) ensures requests come from official app builds.
- **Atomic Transactions:** Progress, rate-limit counters (`failedAttempts`), and lockout timestamps (`lockedUntil`) are read and updated atomically.
- **Progression Check:** Rejects requests if `challengeId` does not match the active `currentChallengeIndex`.
- **Replay Protection:** Rejects requests if `challengeId` is already in `completedChallengeIds`.
- **Escalating Lockout:** 5 consecutive failures triggers a 30-second cooldown; 10 failures triggers a 5-minute lockout.

## Secret Answer Document (`answers/{challengeId}`)

Create `heritage_sites/hampi/hunts/hampi_intro/answers/gopuram`:

```json
{
  "expectedText": "gopuram",
  "caseSensitive": false
}
```

Create `heritage_sites/hampi/hunts/hampi_intro/answers/pillars`:

```json
{
  "target": 12,
  "tolerance": 1
}
```

## Challenge Document (`challenges/{challengeId}`)

Public challenge documents contain metadata, questions, clues, points, and validation type, but **no secret answer keys**:

```json
{
  "goal": "DISCOVER_SYMBOL",
  "question": "What is the tall gateway tower of a South Indian temple called?",
  "interactionType": "TEXT",
  "validation": {
    "type": "TEXT",
    "caseSensitive": false
  },
  "isOptional": false,
  "clues": ["It is the ornate entrance tower."],
  "discovery": {
    "title": "Gateway tower",
    "body": "Gopurams are monumental gateways common in Dravidian temple architecture."
  },
  "points": 20,
  "options": [],
  "order": 1
}
```

## User Progress Document

Progress documents are stored under:

```text
users/{email}/hunt_progress/{siteId}_{huntId}
```

Stores `currentChallengeIndex`, `completedChallengeIds`, `earnedPoints`, `failedAttempts`, `lockedUntil`, and server timestamps.
