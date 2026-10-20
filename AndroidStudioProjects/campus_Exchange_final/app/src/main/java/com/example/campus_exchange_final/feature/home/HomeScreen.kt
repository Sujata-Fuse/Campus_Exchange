package com.example.campus_exchange_final.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.jan.supabase.auth.auth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusBottomNavBar
import com.example.campus_exchange_final.core.ui.NavTab
import com.example.campus_exchange_final.feature.auth.BackgroundLogoWatermark
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BorderLight
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary
import com.example.campus_exchange_final.ui.theme.WarningSurface

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemListing(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String,
    val price: String,
    val category: String,
    val location: String,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("lending_type") val lendingType: String = "Free",
    val duration: String = "",
    val description: String = "",
    val condition: String = "Good"
)

val categories = listOf("All", "Books", "Electronics", "Fashion", "Furniture", "Notes", "Sports")

// Screen 9: Home Dashboard
@Composable
fun HomeScreen(
    onItemClick: (ItemListing) -> Unit,
    onBorrowBannerClick: () -> Unit,
    onLostFoundBannerClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    onChatListClick: () -> Unit = {},
    selectedTab: NavTab = NavTab.HOME,
    onTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = remember { HomeViewModel() }
) {
    LaunchedEffect(Unit) {
        homeViewModel.fetchListings()
        homeViewModel.fetchUserProfile()
    }
    val currentUser = remember {
        try {
            com.example.campus_exchange_final.core.SupabaseProvider.client.auth.currentUserOrNull()
        } catch (_: Exception) {
            null
        }
    }

    val liveProfile = homeViewModel.userProfile

    val fullName = remember(currentUser, liveProfile) {
        if (liveProfile != null && liveProfile.full_name.isNotBlank()) {
            liveProfile.full_name
        } else {
            val metaName = currentUser?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")
            if (!metaName.isNullOrBlank() && metaName != "null") {
                metaName
            } else {
                val email = currentUser?.email
                if (!email.isNullOrBlank()) {
                    email.substringBefore("@").replaceFirstChar { it.uppercase() }
                } else {
                    "Student"
                }
            }
        }
    }

    val firstName = remember(fullName) {
        fullName.substringBefore(" ")
    }

    val initials = remember(fullName) {
        val parts = fullName.trim().split("\\s+".toRegex())
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else if (fullName.isNotEmpty()) {
            fullName.take(2).uppercase()
        } else {
            "ST"
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val displayListings = homeViewModel.listings

    val filteredListings = remember(searchQuery, selectedCategory, displayListings) {
        displayListings.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        BackgroundLogoWatermark(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "$firstName! 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onChatListClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, BorderLight, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Chats",
                                tint = BrandPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, BorderLight, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = BrandPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BrandPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar with dynamic filtering and clear action
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search items, people...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = BrandPurple
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Feature Banners Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Borrow & Lend Banner
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onBorrowBannerClick() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Borrow & Lend",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Explore items",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    // Lost & Found Banner
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clickable { onLostFoundBannerClick() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TealSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Lost & Found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Report or claim",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Categories Header
                Text(
                    text = "Popular Categories",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Chips Horizontal Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BrandPurple else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) BrandPurple else BorderLight,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Recent Listings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Listings Grid or Empty State
                if (filteredListings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No items found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching for a different keyword or category filter.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredListings) { item ->
                            ItemCard(
                                item = item,
                                onClick = { onItemClick(item) }
                            )
                        }
                    }
                }
            }

            // Bottom Navigation
            CampusBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun ItemCard(
    item: ItemListing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleSurface),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = item.category.take(1),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = BrandPurple,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.price,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandPurple
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        color = TextSecondary
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
