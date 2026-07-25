package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Challenge
import com.example.itihasa.engine.domain.ChallengeGoal
import com.example.itihasa.engine.domain.Hunt
import com.example.itihasa.engine.domain.InteractionType
import com.example.itihasa.engine.domain.Validation
import com.example.itihasa.engine.domain.ValidationConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HuntEngineTest {
    @Test
    fun textValidatorTrimsWhitespaceAndIgnoresCase() = runBlocking {
        val validator = TextValidator()
        val result = validator.validate(
            Validation(config = ValidationConfig.TextValidationConfig("Gopuram")),
            UserInput.TextInput("  gopuram  ")
        )

        assertEquals(ValidationResult.Pass, result)
    }

    @Test
    fun numberValidatorAcceptsValueWithinTolerance() = runBlocking {
        val validator = NumberValidator()
        val result = validator.validate(
            Validation(config = ValidationConfig.NumberValidationConfig(target = 12, tolerance = 1)),
            UserInput.NumberInput(13)
        )

        assertEquals(ValidationResult.Pass, result)
    }

    @Test
    fun correctAnswerAdvancesTheHuntAndAwardsPoints() = runBlocking {
        val challenge = Challenge(
            id = "tower_count",
            goal = ChallengeGoal.COUNT_OBJECT,
            question = "How many towers?",
            interactionType = InteractionType.NUMBER,
            validation = Validation(config = ValidationConfig.NumberValidationConfig(target = 4)),
            points = 25
        )
        val engine = HuntEngine()
        val session = engine.start(Hunt(id = "hampi_intro", siteId = "hampi"), listOf(challenge))

        val submission = engine.submit(session, UserInput.NumberInput(4))

        assertEquals(ValidationResult.Pass, submission.result)
        assertTrue(submission.session.isCompleted)
        assertEquals(25, submission.session.earnedPoints)
        assertTrue("tower_count" in submission.session.completedChallengeIds)
    }
}
