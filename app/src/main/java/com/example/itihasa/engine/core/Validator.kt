package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Validation

interface Validator {
    suspend fun validate(validation: Validation, userInput: UserInput): ValidationResult
}
