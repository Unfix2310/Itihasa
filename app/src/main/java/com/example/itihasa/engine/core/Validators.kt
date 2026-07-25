package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Validation
import com.example.itihasa.engine.domain.ValidationConfig

class TextValidator : Validator {
    override suspend fun validate(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult {
        val config = validation.config as? ValidationConfig.TextValidationConfig
            ?: return ValidationResult.Fail("This challenge has an invalid text rule.")
        val submitted = when (userInput) {
            is UserInput.TextInput -> userInput.text
            is UserInput.ChoiceInput -> userInput.choice
            else -> return ValidationResult.Fail("Enter a text answer for this challenge.")
        }
        val expected = normalize(config.expectedText, config.caseSensitive)
        val actual = normalize(submitted, config.caseSensitive)

        return if (actual == expected) {
            ValidationResult.Pass
        } else {
            ValidationResult.Fail("That is not quite right. Try another clue and answer again.")
        }
    }

    private fun normalize(value: String, caseSensitive: Boolean): String {
        val trimmed = value.trim().replace(Regex("\\s+"), " ")
        return if (caseSensitive) trimmed else trimmed.lowercase()
    }
}

class NumberValidator : Validator {
    override suspend fun validate(
        validation: Validation,
        userInput: UserInput
    ): ValidationResult {
        val config = validation.config as? ValidationConfig.NumberValidationConfig
            ?: return ValidationResult.Fail("This challenge has an invalid number rule.")
        val submitted = (userInput as? UserInput.NumberInput)?.number
            ?: return ValidationResult.Fail("Enter a number for this challenge.")

        return if (kotlin.math.abs(submitted - config.target) <= config.tolerance) {
            ValidationResult.Pass
        } else {
            ValidationResult.Fail("That number is outside the allowed range. Check your count and try again.")
        }
    }
}

/** Camera classification is intentionally deferred until a model is integrated. */
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
