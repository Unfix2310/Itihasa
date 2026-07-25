package com.example.itihasa.engine.core

sealed class UserInput {
    data class TextInput(val text: String) : UserInput()
    data class NumberInput(val number: Int) : UserInput()
    data class ChoiceInput(val choice: String) : UserInput()
    data class VisionInput(val bitmap: Any) : UserInput()
    data class GpsInput(val lat: Double, val lng: Double) : UserInput()
}
