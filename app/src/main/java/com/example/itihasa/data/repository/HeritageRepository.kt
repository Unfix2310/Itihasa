package com.example.itihasa.data.repository

import com.example.itihasa.data.model.HeritageSite
import com.example.itihasa.data.model.User
import com.example.itihasa.data.model.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HeritageRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getHeritageSites(): List<HeritageSite> {
        return try {
            db.collection("heritage_sites").get().await().documents.mapNotNull { document ->
                document.toObject(HeritageSite::class.java)?.let { site ->
                    if (site.id.isBlank()) site.copy(id = document.id) else site
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserProfile(): User? {
        val email = auth.currentUser?.email ?: return null
        return try {
            db.collection("users").document(email).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRecentActivities(): List<UserActivity> {
        val email = auth.currentUser?.email ?: return emptyList()
        return try {
            db.collection("users").document(email).collection("activities")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get().await().toObjects(UserActivity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
