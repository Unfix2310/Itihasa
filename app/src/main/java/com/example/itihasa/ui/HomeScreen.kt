package com.example.itihasa.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.itihasa.R
import com.example.itihasa.data.model.HeritageSite
import com.example.itihasa.ui.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onOpenSite: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sites by viewModel.sites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getCurrentLocation(context) { location ->
                viewModel.sortSitesByDistance(location)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
    ) {
        HomeHeader()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Enrolled Sites Section (Large Cards)
            val enrolledSites = sites.filter { it.completedHunts > 0 && !it.completed }
            if (enrolledSites.isNotEmpty()) {
                item {
                    SectionHeader(title = "Your Enrolled Sites")
                }
                items(enrolledSites) { site ->
                    EnrolledSiteCard(site, onOpenSite)
                }
            } else if (!isLoading) {
                item {
                    SectionHeader(title = "Start Your Journey")
                    FeaturedHuntCard(sites.firstOrNull(), onOpenSite)
                }
            }

            // Recommended Section (Horizontal Row)
            if (sites.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recommended for You")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(sites.filter { it.completedHunts == 0 }) { site ->
                            RecommendedCard(site, onOpenSite)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Popular Categories")
                PopularCategoriesRow()
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EnrolledSiteCard(site: HeritageSite, onOpenSite: (String) -> Unit = {}) {
    val progress = if (site.totalHunts > 0) site.completedHunts.toFloat() / site.totalHunts else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable(enabled = site.id.isNotBlank()) { onOpenSite(site.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = site.coverImage.ifEmpty { R.drawable.bg_temple },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFE46419), CircleShape))
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(site.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                Text("${site.completedHunts} of ${site.totalHunts} Hunts Completed", color = Color(0xFF666666), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFE46419),
                    trackColor = Color(0xFFFDE0D0)
                )
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search heritage sites, hunts...", color = Color(0xFF999999)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF666666)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedTextColor = Color(0xFF1A1A1A),
                focusedTextColor = Color(0xFF1A1A1A)
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RecommendedCard(site: HeritageSite, onOpenSite: (String) -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(enabled = site.id.isNotBlank()) { onOpenSite(site.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = site.coverImage.ifEmpty { R.drawable.bg_temple },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(site.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, color = Color(0xFF1A1A1A))
                Text(site.locationName, color = Color(0xFF666666), fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun FeaturedHuntCard(site: HeritageSite?, onOpenSite: (String) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 24.dp)
            .clickable(enabled = !site?.id.isNullOrBlank()) { site?.id?.let(onOpenSite) },
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            AsyncImage(
                model = site?.coverImage?.ifEmpty { R.drawable.bg_temple } ?: R.drawable.bg_temple,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(site?.name ?: "Hampi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Start your first adventure", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PopularCategoriesRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CategoryItem("Temples", R.drawable.ic_launcher_foreground)
        CategoryItem("Forts", R.drawable.ic_launcher_foreground)
        CategoryItem("Palaces", R.drawable.ic_launcher_foreground)
        CategoryItem("Caves", R.drawable.ic_launcher_foreground)
    }
}

@Composable
fun CategoryItem(label: String, iconRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(60.dp).background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color(0xFF4A2C10), modifier = Modifier.size(30.dp))
        }
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), color = Color(0xFF333333))
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (Location) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location)
        }
    }
}
