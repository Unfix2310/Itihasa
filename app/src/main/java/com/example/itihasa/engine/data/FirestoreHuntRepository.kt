package com.example.itihasa.engine.data

import com.example.itihasa.engine.domain.Challenge
import com.example.itihasa.engine.domain.Hunt
import com.example.itihasa.engine.domain.HuntRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHuntRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : HuntRepository {

    override suspend fun getHunts(siteId: String): List<Hunt> =
        firestore.collection("heritage_sites").document(siteId).collection("hunts")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(HuntDto::class.java)?.toDomain(document.id, siteId)
            }
            .sortedBy { it.title }

    override suspend fun getHunt(siteId: String, huntId: String): Hunt? {
        val document = firestore.collection("heritage_sites").document(siteId)
            .collection("hunts").document(huntId).get().await()
        return document.toObject(HuntDto::class.java)?.toDomain(document.id, siteId)
    }

    override suspend fun getChallenges(siteId: String, huntId: String): List<Challenge> =
        firestore.collection("heritage_sites").document(siteId)
            .collection("hunts").document(huntId).collection("challenges")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(ChallengeDto::class.java)?.toDomain(document.id)
            }
            .sortedWith(compareBy<Challenge> { it.order }.thenBy { it.id })
}
