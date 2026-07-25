package com.example.itihasa.engine.core

import com.example.itihasa.engine.domain.Validation

interface Validator {
    suspend fun validate(session: HuntSession, validation: Validation, userInput: UserInput): ValidationResult
}
