package com.example.itihasa.engine.domain

/** Data source contract used by the engine UI without exposing Firestore. */
interface HuntRepository {
    suspend fun getHunts(siteId: String): List<Hunt>
    suspend fun getHunt(siteId: String, huntId: String): Hunt?
    suspend fun getChallenges(siteId: String, huntId: String): List<Challenge>
}

interface HuntProgressRepository {
    suspend fun getProgress(siteId: String, huntId: String): HuntProgress?
    suspend fun saveProgress(progress: HuntProgress)
}
