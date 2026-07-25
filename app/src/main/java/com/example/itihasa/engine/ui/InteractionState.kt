package com.example.itihasa.engine.ui

import com.example.itihasa.engine.core.ValidationResult
import com.example.itihasa.engine.domain.Hunt
import com.example.itihasa.engine.core.HuntSession

sealed class InteractionState {
    data object Idle : InteractionState()
    data object Submitting : InteractionState()
    data class Feedback(
        val result: ValidationResult,
        val pointsEarned: Int = 0
    ) : InteractionState()
}

data class HuntUiState(
    val siteId: String = "",
    val huntId: String = "",
    val isLoading: Boolean = true,
    val hunt: Hunt? = null,
    val session: HuntSession? = null,
    val answer: String = "",
    val interaction: InteractionState = InteractionState.Idle,
    val errorMessage: String? = null,
    val persistenceWarning: String? = null
)

data class HuntCatalogUiState(
    val isLoading: Boolean = true,
    val hunts: List<Hunt> = emptyList(),
    val errorMessage: String? = null
)
