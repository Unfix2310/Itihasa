package com.example.itihasa.engine.domain

sealed class ValidationConfig {
    object Empty : ValidationConfig()
    
    data class TextValidationConfig(
        val expectedText: String, 
        val caseSensitive: Boolean = false
    ) : ValidationConfig()
    
    data class NumberValidationConfig(
        val target: Int, 
        val tolerance: Int = 0
    ) : ValidationConfig()
    
    data class VisionValidationConfig(
        val targetAssetId: String, 
        val confidenceThreshold: Float = 0.8f
    ) : ValidationConfig()
    
    data class GpsValidationConfig(
        val latitude: Double, 
        val longitude: Double, 
        val radiusMeters: Float
    ) : ValidationConfig()
}

data class Validation(
    val type: String = "", 
    val config: ValidationConfig = ValidationConfig.Empty
)
