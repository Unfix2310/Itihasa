package com.example.itihasa.engine.core

sealed class ValidationResult {
    data object Pass : ValidationResult()
    data class Fail(val reason: String, val errorCode: String? = null) : ValidationResult()
    data class Partial(val score: Float, val hint: String) : ValidationResult()
    data object Skip : ValidationResult()
    data object Timeout : ValidationResult()
    data class LockedOut(val waitSeconds: Int, val message: String) : ValidationResult()
    data class OutOfOrder(val message: String) : ValidationResult()
    data class RateLimited(val message: String) : ValidationResult()
    data class NetworkError(val message: String) : ValidationResult()
}
