package com.example.itihasa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.itihasa.ui.viewmodel.HomeViewModel
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: HomeViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Start),
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .border(2.dp, Color(0xFFE46419), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profilePicUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = user?.profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.name ?: "Explorer",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = user?.email ?: "",
            color = Color(0xFF666666),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatItem(
                value = user?.points?.toString() ?: "0",
                label = "Points",
                modifier = Modifier.weight(1f)
            )
            ProfileStatItem(
                value = user?.huntsCompleted?.toString() ?: "0",
                label = "Hunts",
                modifier = Modifier.weight(1f)
            )
            ProfileStatItem(
                value = user?.badgesEarned?.toString() ?: "0",
                label = "Badges",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings / Options
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileOptionItem(Icons.Default.Edit, "Edit Profile") {
                Toast.makeText(context, "Edit profile is not connected yet", Toast.LENGTH_SHORT).show()
            }
            ProfileOptionItem(Icons.Default.History, "My History") {
                Toast.makeText(context, "History view is not connected yet", Toast.LENGTH_SHORT).show()
            }
            ProfileOptionItem(Icons.Default.Settings, "Settings") {
                Toast.makeText(context, "Settings is not connected yet", Toast.LENGTH_SHORT).show()
            }
            ProfileOptionItem(Icons.Default.Logout, "Logout", Color.Red) {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                onLogout()
            }
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE46419))
        Text(label, fontSize = 12.sp, color = Color(0xFF666666))
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9F9F9),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Medium, color = color)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF999999))
        }
    }
}
