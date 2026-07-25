package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Validation
import com.example.itihasa.engine.domain.ValidationConfig

interface AnswerVerifier {
    suspend fun verifyAnswer(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult
}

/**
 * Server-side answer verifier for production.
 * Real answer keys are stored exclusively in deny-all Firestore subcollections
 * and validated server-side by Firebase Cloud Functions (`verifyChallengeAnswer`).
 */
class ServerAnswerVerifier : AnswerVerifier {
    override suspend fun verifyAnswer(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult {
        return ValidationResult.Fail("Answer verification is executed server-side via Cloud Functions.")
    }
}

/**
 * Synthetic mock answer verifier strictly for isolated local testing.
 * Uses mock fake answers only and is never used with production data.
 */
class MockAnswerVerifier(
    private val mockTextAnswers: Map<String, String> = mapOf("TEXT" to "gopuram", "MULTIPLE_CHOICE" to "gopuram"),
    private val mockNumberAnswers: Map<String, Int> = mapOf("NUMBER" to 12)
) : AnswerVerifier {
    override suspend fun verifyAnswer(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult {
        return when (validation.config) {
            is ValidationConfig.TextValidationConfig -> {
                val submitted = when (userInput) {
                    is UserInput.TextInput -> userInput.text
                    is UserInput.ChoiceInput -> userInput.choice
                    else -> return ValidationResult.Fail("Enter a text answer for this challenge.")
                }
                val expected = mockTextAnswers[validation.type] ?: "gopuram"
                val config = validation.config as ValidationConfig.TextValidationConfig
                val isMatch = if (config.caseSensitive) submitted.trim() == expected else submitted.trim().lowercase() == expected.lowercase()
                if (isMatch) ValidationResult.Pass else ValidationResult.Fail("That is not quite right. Try another clue and answer again.")
            }
            is ValidationConfig.NumberValidationConfig -> {
                val submitted = (userInput as? UserInput.NumberInput)?.number
                    ?: return ValidationResult.Fail("Enter a number for this challenge.")
                val target = mockNumberAnswers[validation.type] ?: 12
                if (submitted == target) ValidationResult.Pass else ValidationResult.Fail("That number is outside the allowed range. Check your count and try again.")
            }
            else -> ValidationResult.Fail("Validation unavailable.")
        }
    }
}

class TextValidator(private val verifier: AnswerVerifier = ServerAnswerVerifier()) : Validator {
    override suspend fun validate(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult = verifier.verifyAnswer(validation, userInput)
}

class NumberValidator(private val verifier: AnswerVerifier = ServerAnswerVerifier()) : Validator {
    override suspend fun validate(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult = verifier.verifyAnswer(validation, userInput)
}

class VisionValidator : Validator {
    override suspend fun validate(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult = ValidationResult.Fail(
        "Camera validation is not available yet. Try another challenge."
    )
}

class ValidatorRegistry(
    private val textValidator: Validator = TextValidator(),
    private val numberValidator: Validator = NumberValidator(),
    private val visionValidator: Validator = VisionValidator()
) {
    fun validatorFor(validation: Validation): Validator? = when (validation.config) {
        is ValidationConfig.TextValidationConfig -> textValidator
        is ValidationConfig.NumberValidationConfig -> numberValidator
        is ValidationConfig.VisionValidationConfig -> visionValidator
        else -> null
    }
}
