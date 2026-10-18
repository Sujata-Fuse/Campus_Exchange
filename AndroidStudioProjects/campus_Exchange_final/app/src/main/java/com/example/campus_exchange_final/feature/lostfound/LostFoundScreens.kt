package com.example.campus_exchange_final.feature.lostfound

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusBottomNavBar
import com.example.campus_exchange_final.core.ui.CampusButton
import com.example.campus_exchange_final.core.ui.CampusTextField
import com.example.campus_exchange_final.core.ui.NavTab
import com.example.campus_exchange_final.feature.auth.BackgroundLogoWatermark
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.CoralLost
import com.example.campus_exchange_final.ui.theme.CoralSurface
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealFound
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LostFoundItem(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String,
    @SerialName("is_lost") val isLost: Boolean,
    val category: String,
    val location: String,
    val description: String = "",
    val status: String = "Active",
    val timeAgo: String = "1d ago"
)

val sampleLostFoundItems = listOf(
    LostFoundItem(id = "1", title = "ID Card", isLost = true, category = "Documents", location = "Library • 2 days ago", timeAgo = "2d ago"),
    LostFoundItem(id = "2", title = "Water Bottle", isLost = false, category = "Personal", location = "Hostel 1 • 1 day ago", timeAgo = "1d ago"),
    LostFoundItem(id = "3", title = "Wallet", isLost = true, category = "Accessories", location = "Main Canteen • 3h ago", timeAgo = "3h ago"),
    LostFoundItem(id = "4", title = "Earphones", isLost = false, category = "Electronics", location = "Sports Ground • 5h ago", timeAgo = "5h ago")
)

// Screen 20: Lost & Found Dashboard
@Composable
fun LostFoundDashboardScreen(
    onReportLostClick: () -> Unit,
    onReportFoundClick: () -> Unit,
    onItemClick: (LostFoundItem) -> Unit,
    selectedTab: NavTab = NavTab.LOST_FOUND,
    onTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier,
    lostFoundViewModel: LostFoundViewModel = remember { LostFoundViewModel() }
) {
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

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Lost & Found",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Help each other. Reunite what's lost.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dual Action Cards (Report Lost vs Report Found)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clickable { onReportLostClick() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Report Lost Item",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = CoralLost
                                )
                            )
                            Text(
                                text = "I lost something",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clickable { onReportFoundClick() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = TealSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Report Found Item",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TealFound
                                )
                            )
                            Text(
                                text = "I found something",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Recent Reports",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val displayReports = if (lostFoundViewModel.reports.isNotEmpty()) lostFoundViewModel.reports else sampleLostFoundItems

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayReports) { item ->
                        LostFoundCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }

            CampusBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun LostFoundCard(
    item: LostFoundItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.isLost) CoralSurface else TealSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.take(1),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (item.isLost) CoralLost else TealFound,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.isLost) CoralSurface else TealSurface)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (item.isLost) "Lost" else "Found",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (item.isLost) CoralLost else TealFound,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// Screen 23/24: Report Lost/Found Item Form
@Composable
fun ReportItemScreen(
    isLost: Boolean,
    onSubmitSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    lostFoundViewModel: LostFoundViewModel = remember { LostFoundViewModel() }
) {
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val title = if (isLost) "Report Lost Item" else "Report Found Item"
    val color = if (isLost) CoralLost else TealFound

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            com.example.campus_exchange_final.core.ui.CampusTopBar(title = title, onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = "Item Name",
                placeholder = "e.g. Leather Wallet / ID Card"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = category,
                onValueChange = { category = it },
                label = "Category",
                placeholder = "Documents, Electronics, Accessories..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = location,
                onValueChange = { location = it },
                label = if (isLost) "Last Seen Location" else "Found Location",
                placeholder = "Library, Main Canteen, Hostel..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                placeholder = "Provide distinguishing details..."
            )

            if (lostFoundViewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lostFoundViewModel.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            CampusButton(
                text = if (lostFoundViewModel.isLoading) "Submitting..." else "Submit Report",
                onClick = {
                    lostFoundViewModel.submitReport(
                        title = itemName,
                        isLost = isLost,
                        category = category,
                        location = location,
                        description = description
                    ) {
                        onSubmitSuccess()
                    }
                },
                containerColor = color
            )
        }
    }
}
