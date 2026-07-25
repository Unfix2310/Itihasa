package com.example.itihasa.engine.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.itihasa.engine.core.ValidationResult
import com.example.itihasa.engine.domain.Challenge
import com.example.itihasa.engine.domain.InteractionType
import com.example.itihasa.engine.ui.HuntCatalogViewModel
import com.example.itihasa.engine.ui.HuntViewModel
import com.example.itihasa.engine.ui.InteractionState

private val Ink = Color(0xFF25211C)
private val Terracotta = Color(0xFFB74C28)
private val Sand = Color(0xFFFFF8F0)
private val Moss = Color(0xFF2C6B4A)

@Composable
fun HuntListScreen(
    siteId: String,
    onBack: () -> Unit,
    onStartHunt: (String) -> Unit,
    viewModel: HuntCatalogViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage
    LaunchedEffect(siteId) { viewModel.loadHunts(siteId) }

    Column(modifier = Modifier.fillMaxSize().background(Sand)) {
        HuntTopBar(title = "Choose a Hunt", onBack = onBack)
        when {
            state.isLoading -> LoadingContent()
            errorMessage != null -> ErrorContent(errorMessage, onBack)
            state.hunts.isEmpty() -> EmptyContent(
                "No hunts are published for this site yet.",
                "Add hunt documents under heritage_sites/$siteId/hunts in Firestore."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.hunts, key = { it.id }) { hunt ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onStartHunt(hunt.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(hunt.title, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Ink)
                            if (hunt.description.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(hunt.description, color = Color(0xFF625B52))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "${hunt.totalChallenges} challenges",
                                color = Terracotta,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HuntScreen(
    siteId: String,
    huntId: String,
    onBack: () -> Unit,
    viewModel: HuntViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val errorMessage = state.errorMessage
    val session = state.session
    LaunchedEffect(siteId, huntId) { viewModel.loadHunt(siteId, huntId) }

    Column(modifier = Modifier.fillMaxSize().background(Sand)) {
        HuntTopBar(title = state.hunt?.title ?: "Hunt", onBack = onBack)
        when {
            state.isLoading -> LoadingContent()
            errorMessage != null -> ErrorContent(errorMessage, onBack)
            session == null -> EmptyContent("This hunt is unavailable.", "Try opening it again from the hunt list.")
            session.isCompleted -> CompletionContent(session.earnedPoints, onBack)
            state.interaction is InteractionState.Feedback -> FeedbackContent(
                feedback = state.interaction as InteractionState.Feedback,
                onContinue = viewModel::continueAfterFeedback
            )
            else -> ChallengeContent(
                challenge = session.currentChallenge ?: return@Column,
                answer = state.answer,
                isSubmitting = state.interaction is InteractionState.Submitting,
                earnedPoints = session.earnedPoints,
                currentNumber = session.currentChallengeIndex + 1,
                totalChallenges = session.challenges.size,
                persistenceWarning = state.persistenceWarning,
                onAnswerChanged = viewModel::updateAnswer,
                onSubmit = viewModel::submitAnswer,
                onSkip = viewModel::skipChallenge
            )
        }
    }
}

@Composable
private fun HuntTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

@Composable
private fun ChallengeContent(
    challenge: Challenge,
    answer: String,
    isSubmitting: Boolean,
    earnedPoints: Int,
    currentNumber: Int,
    totalChallenges: Int,
    persistenceWarning: String?,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit
) {
    val progress = currentNumber.toFloat() / totalChallenges.coerceAtLeast(1)
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Challenge $currentNumber of $totalChallenges", color = Color(0xFF625B52))
            Text("$earnedPoints points", color = Terracotta, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Terracotta,
            trackColor = Color(0xFFF0DED0)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(challenge.goal.name.replace('_', ' '), color = Moss, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(challenge.question, color = Ink, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                if (challenge.clues.isNotEmpty()) {
                    Text("Clue: ${challenge.clues.first()}", color = Color(0xFF625B52))
                }
            }
        }

        when (challenge.interactionType) {
            InteractionType.MULTIPLE_CHOICE -> {
                challenge.options.forEach { option ->
                    OutlinedButton(
                        onClick = { onAnswerChanged(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (answer == option) Color(0xFFFBE5D6) else Color.Transparent,
                            contentColor = Ink
                        )
                    ) { Text(option) }
                }
            }
            InteractionType.TEXT, InteractionType.NUMBER -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (challenge.interactionType == InteractionType.NUMBER) "Your number" else "Your answer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (challenge.interactionType == InteractionType.NUMBER) KeyboardType.Number else KeyboardType.Text
                    )
                )
            }
            else -> {
                EmptyContent(
                    "This ${challenge.interactionType.name.lowercase()} challenge is coming soon.",
                    "Text, number, and multiple-choice challenges are ready first."
                )
            }
        }

        if (persistenceWarning != null) {
            Text(persistenceWarning, color = Terracotta, fontSize = 13.sp)
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !isSubmitting && challenge.interactionType in setOf(
                InteractionType.TEXT,
                InteractionType.NUMBER,
                InteractionType.MULTIPLE_CHOICE
            ),
            colors = ButtonDefaults.buttonColors(containerColor = Terracotta)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
            } else {
                Text("Check Answer")
            }
        }
        if (challenge.isOptional) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now")
            }
        }
    }
}

@Composable
private fun FeedbackContent(feedback: InteractionState.Feedback, onContinue: () -> Unit) {
    val (title, body, color) = when (val result = feedback.result) {
        ValidationResult.Pass -> Triple(
            "Correct!",
            if (feedback.pointsEarned > 0) "+${feedback.pointsEarned} points earned" else "Great observation.",
            Moss
        )
        is ValidationResult.Fail -> Triple("Try again", result.reason, Terracotta)
        is ValidationResult.Partial -> Triple("Almost there", result.hint, Terracotta)
        ValidationResult.Skip -> Triple("Skipped", "You can return to this optional challenge later.", Color(0xFF625B52))
        ValidationResult.Timeout -> Triple("Time is up", "Try this challenge again when you are ready.", Terracotta)
    }
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, color = color, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(body, color = Ink)
                Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = color)) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun CompletionContent(points: Int, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Hunt complete", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Moss)
                Text("You earned $points points in this hunt.", color = Ink)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Terracotta)) {
                    Text("Back to Hunts")
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terracotta)
    }
}

@Composable
private fun ErrorContent(message: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = Ink)
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Terracotta)) {
                Text("Go back")
            }
        }
    }
}

@Composable
private fun EmptyContent(title: String, detail: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Ink)
            Text(detail, color = Color(0xFF625B52))
        }
    }
}
