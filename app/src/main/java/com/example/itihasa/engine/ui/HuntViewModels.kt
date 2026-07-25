package com.example.itihasa.engine.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itihasa.engine.core.HuntEngine
import com.example.itihasa.engine.core.UserInput
import com.example.itihasa.engine.core.ValidationResult
import com.example.itihasa.engine.domain.HuntProgressRepository
import com.example.itihasa.engine.domain.HuntRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HuntCatalogViewModel(
    private val repository: HuntRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HuntCatalogUiState())
    val uiState: StateFlow<HuntCatalogUiState> = _uiState.asStateFlow()

    fun loadHunts(siteId: String) {
        viewModelScope.launch {
            _uiState.value = HuntCatalogUiState(isLoading = true)
            try {
                _uiState.value = HuntCatalogUiState(hunts = repository.getHunts(siteId), isLoading = false)
            } catch (error: Exception) {
                _uiState.value = HuntCatalogUiState(
                    isLoading = false,
                    errorMessage = "We could not load this site's hunts. Please try again."
                )
            }
        }
    }
}

class HuntViewModel(
    private val repository: HuntRepository,
    private val progressRepository: HuntProgressRepository,
    private val engine: HuntEngine = HuntEngine()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HuntUiState())
    val uiState: StateFlow<HuntUiState> = _uiState.asStateFlow()

    fun loadHunt(siteId: String, huntId: String) {
        val current = _uiState.value
        if (current.siteId == siteId && current.huntId == huntId && current.hunt != null) return

        viewModelScope.launch {
            _uiState.value = HuntUiState(siteId = siteId, huntId = huntId, isLoading = true)
            try {
                val hunt = repository.getHunt(siteId, huntId)
                    ?: throw IllegalStateException("Hunt not found")
                val challenges = repository.getChallenges(siteId, huntId)
                if (challenges.isEmpty()) throw IllegalStateException("Hunt has no challenges")

                val progress = progressRepository.getProgress(siteId, huntId)
                _uiState.value = HuntUiState(
                    siteId = siteId,
                    huntId = huntId,
                    isLoading = false,
                    hunt = hunt,
                    session = engine.start(hunt, challenges, progress)
                )
            } catch (error: Exception) {
                _uiState.value = HuntUiState(
                    siteId = siteId,
                    huntId = huntId,
                    isLoading = false,
                    errorMessage = "We could not open this hunt. Check the Firestore data and try again."
                )
            }
        }
    }

    fun updateAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(answer = answer)
    }

    fun submitAnswer() {
        val state = _uiState.value
        val challenge = state.session?.currentChallenge ?: return
        val input = when (challenge.interactionType) {
            com.example.itihasa.engine.domain.InteractionType.NUMBER -> {
                val number = state.answer.trim().toIntOrNull()
                    ?: run {
                        showFeedback(ValidationResult.Fail("Enter a whole number to continue."))
                        return
                    }
                UserInput.NumberInput(number)
            }
            com.example.itihasa.engine.domain.InteractionType.MULTIPLE_CHOICE -> {
                if (state.answer.isBlank()) {
                    showFeedback(ValidationResult.Fail("Choose an answer to continue."))
                    return
                }
                UserInput.ChoiceInput(state.answer)
            }
            com.example.itihasa.engine.domain.InteractionType.TEXT -> {
                if (state.answer.isBlank()) {
                    showFeedback(ValidationResult.Fail("Enter an answer to continue."))
                    return
                }
                UserInput.TextInput(state.answer)
            }
            else -> {
                showFeedback(ValidationResult.Fail("This challenge type is not available yet."))
                return
            }
        }
        submit(input)
    }

    fun skipChallenge() {
        val session = _uiState.value.session ?: return
        val submission = engine.skip(session)
        applySubmission(submission.session, submission.result)
    }

    fun continueAfterFeedback() {
        _uiState.value = _uiState.value.copy(
            answer = "",
            interaction = InteractionState.Idle,
            persistenceWarning = null
        )
    }

    private fun submit(input: UserInput) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(interaction = InteractionState.Submitting)
            val submission = engine.submit(session, input)
            applySubmission(submission.session, submission.result)
        }
    }

    private fun applySubmission(
        session: com.example.itihasa.engine.core.HuntSession,
        result: ValidationResult
    ) {
        viewModelScope.launch {
            var warning: String? = null
            if (result is ValidationResult.Pass || result is ValidationResult.Skip) {
                try {
                    progressRepository.saveProgress(session.toProgress())
                } catch (error: Exception) {
                    warning = "Your answer is saved on this screen, but could not sync yet."
                }
            }
            _uiState.value = _uiState.value.copy(
                session = session,
                interaction = InteractionState.Feedback(
                    result = result,
                    pointsEarned = if (result is ValidationResult.Pass) {
                        session.challenges.getOrNull(session.currentChallengeIndex - 1)?.points ?: 0
                    } else {
                        0
                    }
                ),
                persistenceWarning = warning
            )
        }
    }

    private fun showFeedback(result: ValidationResult) {
        _uiState.value = _uiState.value.copy(interaction = InteractionState.Feedback(result))
    }
}
