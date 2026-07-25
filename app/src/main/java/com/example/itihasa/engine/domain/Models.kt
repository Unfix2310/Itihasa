package com.example.itihasa.engine.domain

data class Discovery(
    val title: String = "",
    val body: String = "",
    val imageAssetId: String? = null,
    val fact: String? = null,
    val reference: String? = null
)

data class ChallengeMetadata(
    val difficulty: Difficulty = Difficulty.EASY,
    val estimatedTimeMinutes: Int = 0,
    val concept: String = "",
    val tags: List<String> = emptyList(),
    val version: Int = 1,
    val author: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Challenge(
    val id: String = "",
    val goal: ChallengeGoal = ChallengeGoal.DISCOVER_SYMBOL,
    val question: String = "",
    val interactionType: InteractionType = InteractionType.TEXT,
    val validation: Validation = Validation(),
    val isOptional: Boolean = false,
    val clues: List<String> = emptyList(),
    val discovery: Discovery = Discovery(),
    val points: Int = 0,
    val metadata: ChallengeMetadata = ChallengeMetadata(),
    val options: List<String> = emptyList(),
    val order: Int = 0
)

data class Hunt(
    val id: String = "",
    val siteId: String = "",
    val title: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    val totalChallenges: Int = 0
)

data class HuntProgress(
    val siteId: String,
    val huntId: String,
    val currentChallengeIndex: Int = 0,
    val completedChallengeIds: Set<String> = emptySet(),
    val earnedPoints: Int = 0,
    val isCompleted: Boolean = false
)
