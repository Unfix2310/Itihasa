package com.example.itihasa.engine.core

sealed class ValidationResult {
    data object Pass : ValidationResult()
    data class Fail(val reason: String) : ValidationResult()
    data class Partial(val score: Float, val hint: String) : ValidationResult()
    data object Skip : ValidationResult()
    data object Timeout : ValidationResult()
}
