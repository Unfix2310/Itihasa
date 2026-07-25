package com.example.itihasa.engine.data

import com.example.itihasa.engine.domain.Challenge
import com.example.itihasa.engine.domain.ChallengeGoal
import com.example.itihasa.engine.domain.ChallengeMetadata
import com.example.itihasa.engine.domain.Difficulty
import com.example.itihasa.engine.domain.Discovery
import com.example.itihasa.engine.domain.Hunt
import com.example.itihasa.engine.domain.InteractionType
import com.example.itihasa.engine.domain.Validation
import com.example.itihasa.engine.domain.ValidationConfig

fun HuntDto.toDomain(id: String, siteId: String) = Hunt(
    id = id,
    siteId = siteId,
    title = title,
    description = description,
    coverImageUrl = coverImageUrl,
    totalChallenges = totalChallenges
)

fun ChallengeDto.toDomain(id: String) = Challenge(
    id = id,
    goal = goal.toEnumOrDefault(ChallengeGoal.DISCOVER_SYMBOL),
    question = question,
    interactionType = interactionType.toEnumOrDefault(InteractionType.TEXT),
    validation = validation.toDomain(),
    isOptional = isOptional,
    clues = clues,
    discovery = discovery.toDomain(),
    points = points,
    metadata = metadata.toDomain(),
    options = options,
    order = order
)

private fun ValidationDto.toDomain(): Validation {
    val normalizedType = type.trim().uppercase()
    val config = when (normalizedType) {
        "TEXT", "MULTIPLE_CHOICE" -> ValidationConfig.TextValidationConfig(
            caseSensitive = caseSensitive
        )
        "NUMBER" -> ValidationConfig.NumberValidationConfig
        "VISION" -> ValidationConfig.VisionValidationConfig(targetAssetId, confidenceThreshold)
        "GPS" -> ValidationConfig.GpsValidationConfig(radiusMeters)
        else -> ValidationConfig.Empty
    }
    return Validation(type = normalizedType, config = config)
}

private fun DiscoveryDto.toDomain() = Discovery(
    title = title,
    body = body,
    imageAssetId = imageAssetId,
    fact = fact,
    reference = reference
)

private fun ChallengeMetadataDto.toDomain() = ChallengeMetadata(
    difficulty = difficulty.toEnumOrDefault(Difficulty.EASY),
    estimatedTimeMinutes = estimatedTimeMinutes,
    concept = concept,
    tags = tags,
    version = version,
    author = author,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    enumValues<T>().firstOrNull { it.name == trim().uppercase() } ?: default
