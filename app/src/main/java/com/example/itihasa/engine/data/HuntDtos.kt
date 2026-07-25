package com.example.itihasa.engine.data

/**
 * Firestore-friendly shapes. Keep these separate from the sealed domain validation model,
 * because Firestore cannot deserialize sealed classes directly.
 */
data class HuntDto(
    val title: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    val totalChallenges: Int = 0
)

data class ChallengeDto(
    val goal: String = "",
    val question: String = "",
    val interactionType: String = "",
    val validation: ValidationDto = ValidationDto(),
    val isOptional: Boolean = false,
    val clues: List<String> = emptyList(),
    val discovery: DiscoveryDto = DiscoveryDto(),
    val points: Int = 0,
    val metadata: ChallengeMetadataDto = ChallengeMetadataDto(),
    val options: List<String> = emptyList(),
    val order: Int = 0
)

data class ValidationDto(
    val type: String = "",
    val expectedText: String = "",
    val caseSensitive: Boolean = false,
    val target: Int = 0,
    val tolerance: Int = 0,
    val targetAssetId: String = "",
    val confidenceThreshold: Float = 0.8f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Float = 0f
)

data class DiscoveryDto(
    val title: String = "",
    val body: String = "",
    val imageAssetId: String? = null,
    val fact: String? = null,
    val reference: String? = null
)

data class ChallengeMetadataDto(
    val difficulty: String = "EASY",
    val estimatedTimeMinutes: Int = 0,
    val concept: String = "",
    val tags: List<String> = emptyList(),
    val version: Int = 1,
    val author: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
