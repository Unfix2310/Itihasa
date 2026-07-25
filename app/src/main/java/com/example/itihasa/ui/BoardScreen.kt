package com.example.itihasa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.example.itihasa.R
import com.example.itihasa.ui.viewmodel.HomeViewModel

@Composable
fun BoardScreen(viewModel: HomeViewModel = viewModel()) {
    val user by viewModel.user.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                tint = Color.Unspecified,
                modifier = Modifier.height(28.dp)
            )
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Black)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    "Your Progress",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A1A)
                )
            }

            item {
                ProgressSection(user)
            }

            item {
                SectionHeader(title = "Badges Earned")
                BadgesSection(user?.badgesEarned ?: 0)
            }

            item {
                DailyChallengeCard {
                    Toast.makeText(context, "Daily Challenge is coming soon", Toast.LENGTH_SHORT).show()
                }
            }

            item {
                InviteFriendsCard {
                    Toast.makeText(context, "Invite flow is not connected yet", Toast.LENGTH_SHORT).show()
                }
            }

            if (activities.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recent Activity")
                }
                items(activities) { activity ->
                    ActivityItem(activity)
                }
            }
        }
    }
}

private fun Modifier.fillWeight(weight: Float): Modifier = this.then(Modifier.fillMaxHeight().fillMaxWidth())
