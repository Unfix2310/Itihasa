package com.example.itihasa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.itihasa.R
import com.example.itihasa.data.model.User
import com.example.itihasa.data.model.UserActivity

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
        Text("See All", color = Color(0xFFE46419), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProgressSection(user: User?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressItem(user?.huntsCompleted?.toString() ?: "0", "Hunts Completed", Icons.Default.Adjust, Color(0xFFE46419))
        ProgressItem(user?.badgesEarned?.toString() ?: "0", "Badges Earned", Icons.Default.EmojiEvents, Color(0xFFFFB74D))
        ProgressItem(user?.points?.toString() ?: "0", "Total Points", Icons.Default.Copyright, Color(0xFFFFD54F))
    }
}

@Composable
fun ProgressItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A1A))
            Text(label, fontSize = 10.sp, color = Color(0xFF666666), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun BadgesSection(badgesCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BadgeItem("Explorer", Icons.Default.Verified, if (badgesCount >= 1) Color(0xFF1E88E5) else Color.LightGray)
        BadgeItem("History Buff", Icons.Default.Star, if (badgesCount >= 5) Color(0xFFE53935) else Color.LightGray)
        BadgeItem("Quick Learner", Icons.Default.Shield, if (badgesCount >= 10) Color(0xFF43A047) else Color.LightGray)
        BadgeItem("Top Performer", Icons.Default.EmojiEvents, if (badgesCount >= 20) Color(0xFFFB8C00) else Color.LightGray)
    }
}

@Composable
fun BadgeItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        }
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp), color = Color(0xFF333333))
    }
}

@Composable
fun DailyChallengeCard(onStartNow: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFE46419))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Challenge", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE46419))
                Text("Answer 5 questions correctly\nWin 50 Points", fontSize = 12.sp, color = Color.Black)
            }
            Button(
                onClick = onStartNow,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE46419)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Start Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InviteFriendsCard(onInviteNow: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F2)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Invite Friends", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                Text("Explore together & earn more points!", fontSize = 12.sp, color = Color(0xFF666666))
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onInviteNow,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE46419)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Invite Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun ActivityItem(activity: UserActivity) {
    val (icon, color) = when (activity.type) {
        "badge_earned" -> Icons.Default.EmojiEvents to Color(0xFFFFB74D)
        else -> Icons.Default.Adjust to Color(0xFFE46419)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A1A))
            Text("2h ago", color = Color(0xFF666666), fontSize = 12.sp) // Simplified
        }
        Text(activity.points, fontWeight = FontWeight.Bold, color = Color(0xFFE46419), fontSize = 14.sp)
    }
}
