package com.example.itihasa.data.model

import com.google.firebase.firestore.GeoPoint

data class HeritageSite(
    val id: String = "",
    val name: String = "",
    val locationName: String = "",
    val geoPoint: GeoPoint? = null,
    val description: String = "",
    val coverImage: String = "",
    val category: String = "",
    val totalHunts: Int = 0,
    val completedHunts: Int = 0,
    var distance: Float = 0f,
    val completed: Boolean = false
)
