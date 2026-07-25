package com.example.itihasa.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itihasa.data.model.HeritageSite
import com.example.itihasa.data.model.User
import com.example.itihasa.data.model.UserActivity
import com.example.itihasa.data.repository.HeritageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = HeritageRepository()

    private val _sites = MutableStateFlow<List<HeritageSite>>(emptyList())
    val sites: StateFlow<List<HeritageSite>> = _sites.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities: StateFlow<List<UserActivity>> = _activities.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sites.value = repository.getHeritageSites()
                _user.value = repository.getUserProfile()
                _activities.value = repository.getRecentActivities()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sortSitesByDistance(userLocation: Location) {
        val currentSites = _sites.value
        val sorted = currentSites.map { site ->
            val distance = FloatArray(1)
            site.geoPoint?.let { gp ->
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    gp.latitude, gp.longitude,
                    distance
                )
                site.copy(distance = distance[0] / 1000f)
            } ?: site
        }.sortedBy { it.distance }
        _sites.value = sorted
    }
}
