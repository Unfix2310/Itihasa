package com.example.itihasa.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicUrl: String = "",
    val role: String = "explorer",
    val lastLogin: Timestamp? = null,
    val points: Int = 0,
    val huntsCompleted: Int = 0,
    val badgesEarned: Int = 0,
    val activeHunts: List<String> = emptyList()
)

data class UserActivity(
    val id: String = "",
    val title: String = "",
    val timestamp: Timestamp? = null,
    val points: String = "",
    val type: String = "", // e.g., "hunt_completed", "badge_earned"
    val siteId: String? = null
)
