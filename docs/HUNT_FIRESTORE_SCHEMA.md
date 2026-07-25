# Hunt Firestore Data

The app now reads published hunts from this path:

```text
heritage_sites/{siteId}/hunts/{huntId}
heritage_sites/{siteId}/hunts/{huntId}/challenges/{challengeId}
```

The site ID is the Firestore document ID from `heritage_sites`. The app now uses that document ID even if the site document has no `id` field.

## Hunt document

Create a document such as `heritage_sites/hampi/hunts/hampi_intro`:

```json
{
  "title": "Hampi Temple Trail",
  "description": "Discover the stories hidden in the stone temples.",
  "coverImageUrl": "",
  "totalChallenges": 2
}
```

## Text challenge

Create `heritage_sites/hampi/hunts/hampi_intro/challenges/gopuram`:

```json
{
  "goal": "DISCOVER_SYMBOL",
  "question": "What is the tall gateway tower of a South Indian temple called?",
  "interactionType": "TEXT",
  "validation": {
    "type": "TEXT",
    "expectedText": "gopuram",
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
  "order": 1,
  "metadata": {
    "difficulty": "EASY",
    "estimatedTimeMinutes": 1,
    "concept": "Dravidian architecture",
    "tags": ["temple", "architecture"],
    "version": 1,
    "author": "Itihasa",
    "createdAt": 0,
    "updatedAt": 0
  }
}
```

## Number challenge

Create `heritage_sites/hampi/hunts/hampi_intro/challenges/pillars`:

```json
{
  "goal": "COUNT_OBJECT",
  "question": "How many pillars can you count in the marked hall?",
  "interactionType": "NUMBER",
  "validation": {
    "type": "NUMBER",
    "target": 12,
    "tolerance": 1
  },
  "isOptional": false,
  "clues": ["Count carefully from left to right."],
  "points": 25,
  "options": [],
  "order": 2,
  "metadata": {
    "difficulty": "MEDIUM",
    "estimatedTimeMinutes": 2,
    "concept": "Observation",
    "tags": ["counting"],
    "version": 1,
    "author": "Itihasa",
    "createdAt": 0,
    "updatedAt": 0
  }
}
```

`MULTIPLE_CHOICE` also works with a `TEXT` validation rule and a non-empty `options` list. The mapper accepts `VISION` and `GPS` validation fields, but the hunt screen deliberately keeps camera and GPS interactions disabled until those features are built.

When a signed-in user progresses, the app creates or updates:

```text
users/{email}/hunt_progress/{siteId}_{huntId}
```

That progress document stores the current challenge index, completed challenge IDs, earned points, and timestamps. It does not yet add points or completed-hunt totals to the parent `users/{email}` profile document.
