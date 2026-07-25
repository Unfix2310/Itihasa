package com.example.itihasa.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.itihasa.engine.data.FirestoreHuntProgressRepository
import com.example.itihasa.engine.data.FirestoreHuntRepository
import com.example.itihasa.engine.domain.HuntProgressRepository
import com.example.itihasa.engine.domain.HuntRepository
import com.example.itihasa.engine.ui.HuntCatalogViewModel
import com.example.itihasa.engine.ui.HuntViewModel

/** App-level composition point: this is the only place that joins UI with Firestore data. */
class HuntEngineViewModelFactory(
    private val huntRepository: HuntRepository = FirestoreHuntRepository(),
    private val progressRepository: HuntProgressRepository = FirestoreHuntProgressRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HuntCatalogViewModel::class.java) -> {
            HuntCatalogViewModel(huntRepository) as T
        }
        modelClass.isAssignableFrom(HuntViewModel::class.java) -> {
            HuntViewModel(huntRepository, progressRepository) as T
        }
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
