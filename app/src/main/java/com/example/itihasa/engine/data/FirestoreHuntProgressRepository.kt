package com.example.itihasa.engine.data

import com.example.itihasa.engine.domain.HuntProgress
import com.example.itihasa.engine.domain.HuntProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreHuntProgressRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : HuntProgressRepository {

    override suspend fun getProgress(siteId: String, huntId: String): HuntProgress? {
        val document = progressDocument(siteId, huntId)?.get()?.await() ?: return null
        if (!document.exists()) return null
        return HuntProgress(
            siteId = document.getString("siteId") ?: siteId,
            huntId = document.getString("huntId") ?: huntId,
            currentChallengeIndex = (document.getLong("currentChallengeIndex") ?: 0L).toInt(),
            completedChallengeIds = (document.get("completedChallengeIds") as? List<*>)
                ?.filterIsInstance<String>()
                ?.toSet()
                .orEmpty(),
            earnedPoints = (document.getLong("earnedPoints") ?: 0L).toInt(),
            isCompleted = document.getBoolean("isCompleted") ?: false
        )
    }

    override suspend fun saveProgress(progress: HuntProgress) {
        val document = progressDocument(progress.siteId, progress.huntId)
            ?: return
        firestore.runTransaction { transaction ->
            val existing = transaction.get(document)
            val fields = mutableMapOf<String, Any>(
                "siteId" to progress.siteId,
                "huntId" to progress.huntId,
                "currentChallengeIndex" to progress.currentChallengeIndex,
                "completedChallengeIds" to progress.completedChallengeIds.toList(),
                "earnedPoints" to progress.earnedPoints,
                "isCompleted" to progress.isCompleted,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists()) fields["startedAt"] = FieldValue.serverTimestamp()
            transaction.set(document, fields, SetOptions.merge())
        }.await()
    }

    private fun progressDocument(siteId: String, huntId: String) = auth.currentUser?.email?.let { email ->
        firestore.collection("users").document(email)
            .collection("hunt_progress").document("${siteId}_${huntId}")
    }
}
