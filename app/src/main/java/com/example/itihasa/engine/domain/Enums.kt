package com.example.itihasa.engine.domain

enum class InteractionType {
    TEXT,
    NUMBER,
    VISION,
    GPS,
    MULTIPLE_CHOICE
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

enum class ChallengeGoal {
    DISCOVER_SYMBOL,
    OBSERVE_PATTERN,
    COUNT_OBJECT,
    COMPARE,
    VERIFY_LOCATION
}
