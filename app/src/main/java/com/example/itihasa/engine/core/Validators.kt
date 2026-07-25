package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Validation
import com.example.itihasa.engine.domain.ValidationConfig
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

interface AnswerVerifier {
    suspend fun verifyAnswer(
        session: HuntSession,
        validation: Validation,
        userInput: UserInput
    ): ValidationResult
}

/**
 * Server-side answer verifier for production.
 * Answer checking is performed by Firebase Cloud Function `verifyChallengeAnswer` behind App Check.
 */
class ServerAnswerVerifier(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : AnswerVerifier {
    override suspend fun verifyAnswer(
        session: HuntSession,
        validation: Validation,
        userInput: UserInput
    ): ValidationResult {
        val challenge = session.currentChallenge ?: return ValidationResult.Fail("No active challenge.")
        
        val inputData = when (userInput) {
            is UserInput.TextInput -> userInput.text
            is UserInput.ChoiceInput -> userInput.choice
            is UserInput.NumberInput -> userInput.number
            else -> return ValidationResult.Fail("Unsupported input type.")
        }
        
        val data = hashMapOf(
            "siteId" to session.hunt.siteId,
            "huntId" to session.hunt.id,
            "challengeId" to challenge.id,
            "userInput" to inputData
        )

        return try {
            val result = functions.getHttpsCallable("verifyChallengeAnswer").call(data).await()
            val response = result.getData() as? Map<String, Any> ?: return ValidationResult.Fail("Invalid server response")
            
            val isCorrect = response["isCorrect"] as? Boolean ?: false
            if (isCorrect) {
                ValidationResult.Pass
            } else {
                val errorCode = response["errorCode"] as? String
                val message = response["message"] as? String ?: "That is not quite right. Try again!"
                when (errorCode) {
                    "LOCKED_OUT" -> {
                        val lockedUntil = (response["lockedUntil"] as? Number)?.toLong() ?: 0L
                        val waitSeconds = Math.max(0, ((lockedUntil - System.currentTimeMillis()) / 1000).toInt())
                        ValidationResult.LockedOut(waitSeconds, message)
                    }
                    "OUT_OF_ORDER" -> ValidationResult.OutOfOrder(message)
                    "ALREADY_COMPLETED" -> ValidationResult.Fail(message, errorCode)
                    else -> ValidationResult.Fail(message, errorCode)
                }
            }
        } catch (e: Exception) {
            if (e is FirebaseFunctionsException) {
                ValidationResult.NetworkError(e.message ?: "Server error")
            } else {
                ValidationResult.NetworkError("Check your connection and try again.")
            }
        }
    }
}

/**
 * Synthetic mock answer verifier strictly for isolated local testing.
 */
class MockAnswerVerifier(
    private val mockTextAnswers: Map<String, String> = mapOf("TEXT" to "gopuram", "MULTIPLE_CHOICE" to "gopuram"),
    private val mockNumberAnswers: Map<String, Int> = mapOf("NUMBER" to 12)
) : AnswerVerifier {
    override suspend fun verifyAnswer(
        session: HuntSession,
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
        session: HuntSession,
        validation: Validation,
        userInput: UserInput
    ): ValidationResult = verifier.verifyAnswer(session, validation, userInput)
}

class NumberValidator(private val verifier: AnswerVerifier = ServerAnswerVerifier()) : Validator {
    override suspend fun validate(
        session: HuntSession,
        validation: Validation,
        userInput: UserInput
    ): ValidationResult = verifier.verifyAnswer(session, validation, userInput)
}

class VisionValidator : Validator {
    override suspend fun validate(
        session: HuntSession,
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
