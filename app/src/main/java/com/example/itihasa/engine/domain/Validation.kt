package com.example.itihasa.engine.domain

sealed class ValidationConfig {
    object Empty : ValidationConfig()
    
    data class TextValidationConfig(
        val caseSensitive: Boolean = false
    ) : ValidationConfig()
    
    object NumberValidationConfig : ValidationConfig()
    
    data class VisionValidationConfig(
        val targetAssetId: String, 
        val confidenceThreshold: Float = 0.8f
    ) : ValidationConfig()
    
    data class GpsValidationConfig(
        val radiusMeters: Float
    ) : ValidationConfig()
}

data class Validation(
    val type: String = "", 
    val config: ValidationConfig = ValidationConfig.Empty
)
