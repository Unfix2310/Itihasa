package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Challenge
import com.example.itihasa.engine.domain.Hunt
import com.example.itihasa.engine.domain.HuntProgress

data class HuntSession(
    val hunt: Hunt,
    val challenges: List<Challenge>,
    val currentChallengeIndex: Int = 0,
    val completedChallengeIds: Set<String> = emptySet(),
    val earnedPoints: Int = 0
) {
    val currentChallenge: Challenge?
        get() = challenges.getOrNull(currentChallengeIndex)

    val isCompleted: Boolean
        get() = currentChallengeIndex >= challenges.size

    fun toProgress(): HuntProgress = HuntProgress(
        siteId = hunt.siteId,
        huntId = hunt.id,
        currentChallengeIndex = currentChallengeIndex,
        completedChallengeIds = completedChallengeIds,
        earnedPoints = earnedPoints,
        isCompleted = isCompleted
    )
}

data class HuntSubmission(
    val session: HuntSession,
    val result: ValidationResult
)

class HuntEngine(
    private val validators: ValidatorRegistry = ValidatorRegistry()
) {
    fun start(
        hunt: Hunt,
        challenges: List<Challenge>,
        savedProgress: HuntProgress? = null
    ): HuntSession {
        val orderedChallenges = challenges.sortedWith(compareBy<Challenge> { it.order }.thenBy { it.id })
        val savedIndex = savedProgress?.currentChallengeIndex ?: 0
        return HuntSession(
            hunt = hunt,
            challenges = orderedChallenges,
            currentChallengeIndex = savedIndex.coerceIn(0, orderedChallenges.size),
            completedChallengeIds = savedProgress?.completedChallengeIds.orEmpty(),
            earnedPoints = savedProgress?.earnedPoints ?: 0
        )
    }

    suspend fun submit(session: HuntSession, input: UserInput): HuntSubmission {
        val challenge = session.currentChallenge
            ?: return HuntSubmission(session, ValidationResult.Fail("This hunt is already complete."))
        val validator = validators.validatorFor(challenge.validation)
            ?: return HuntSubmission(
                session,
                ValidationResult.Fail("This challenge type is not available yet.")
            )
        val result = validator.validate(session, challenge.validation, input)
        return when (result) {
            ValidationResult.Pass -> HuntSubmission(
                session = session.copy(
                    currentChallengeIndex = session.currentChallengeIndex + 1,
                    completedChallengeIds = session.completedChallengeIds + challenge.id,
                    earnedPoints = session.earnedPoints + challenge.points
                ),
                result = result
            )
            else -> HuntSubmission(session, result)
        }
    }

    fun skip(session: HuntSession): HuntSubmission {
        val challenge = session.currentChallenge
            ?: return HuntSubmission(session, ValidationResult.Fail("This hunt is already complete."))
        if (!challenge.isOptional) {
            return HuntSubmission(session, ValidationResult.Fail("This challenge must be completed."))
        }
        return HuntSubmission(
            session = session.copy(currentChallengeIndex = session.currentChallengeIndex + 1),
            result = ValidationResult.Skip
        )
    }
}
