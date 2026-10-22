package com.example.campus_exchange_final.feature.activity

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusBottomNavBar
import com.example.campus_exchange_final.core.ui.NavTab
import com.example.campus_exchange_final.feature.auth.BackgroundLogoWatermark
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.CoralLost
import com.example.campus_exchange_final.ui.theme.CoralSurface
import com.example.campus_exchange_final.ui.theme.LightBlue
import com.example.campus_exchange_final.ui.theme.LightBlueSurface
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealFound
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

// Screen 31: Activity Dashboard
@Composable
fun ActivityScreen(
    onActivityClick: (ActivityItem) -> Unit = {},
    onOpenChat: (ActivityItem) -> Unit = {},
    selectedTab: NavTab = NavTab.ACTIVITY,
    onTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier,
    activityViewModel: ActivityViewModel = remember { ActivityViewModel() }
) {
    LaunchedEffect(Unit) {
        activityViewModel.fetchActivities()
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

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Activity",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                val displayActivities = activityViewModel.activities
                val borrowCount = displayActivities.count { it.type == "borrow" }
                val matchesCount = displayActivities.count { it.type == "lost_found" || it.type == "lost_report" || it.type == "found_report" }
                val alertsCount = displayActivities.count { it.type == "item_listed" || it.type == "notification" || it.status == "Pending" }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(borrowCount.toString(), "Requests", PurpleSurface, BrandPurple, Modifier.weight(1f))
                    MetricCard(matchesCount.toString(), "Matches", TealSurface, TealFound, Modifier.weight(1f))
                    MetricCard(alertsCount.toString(), "Alerts", PurpleSurface, BrandPurple, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (activityViewModel.isLoading && displayActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPurple)
                    }
                } else if (activityViewModel.errorMessage != null && displayActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = activityViewModel.errorMessage ?: "Failed to load activities",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CoralLost)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { activityViewModel.fetchActivities() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (displayActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent activity found.\nItems you lend, requests you send or receive, and reports will appear here.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayActivities) { item ->
                            val iconBg = when (item.type) {
                                "borrow" -> if (item.canAcceptReject) PurpleSurface else LightBlueSurface
                                "item_listed" -> PurpleSurface
                                "lost_report" -> CoralSurface
                                "found_report", "lost_found" -> TealSurface
                                else -> PurpleSurface
                            }
                            val iconTint = when (item.type) {
                                "borrow" -> if (item.canAcceptReject) BrandPurple else LightBlue
                                "item_listed" -> BrandPurple
                                "lost_report" -> CoralLost
                                "found_report", "lost_found" -> TealFound
                                else -> BrandPurple
                            }
                            val cardIcon = when (item.type) {
                                "item_listed" -> Icons.Default.AddCircle
                                "borrow" -> Icons.Default.ShoppingBag
                                "lost_report" -> Icons.Default.Search
                                "found_report", "lost_found" -> Icons.Default.CheckCircle
                                else -> Icons.Default.Notifications
                            }

                            val statusBg = when (item.status.lowercase()) {
                                "accepted", "active" -> TealSurface
                                "pending" -> PurpleSurface
                                "rejected", "cancelled" -> CoralSurface
                                else -> PurpleSurface
                            }
                            val statusColor = when (item.status.lowercase()) {
                                "accepted", "active" -> TealFound
                                "pending" -> BrandPurple
                                "rejected", "cancelled" -> CoralLost
                                else -> TextSecondary
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onActivityClick(item) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = cardIcon,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = TextPrimary
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.subtitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(horizontalAlignment = Alignment.End) {
                                            // Status badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(statusBg)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.status,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = statusColor,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.timeAgo,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }

                                    if (item.canAcceptReject && item.requestId.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (item.type == "lost_found") {
                                                        activityViewModel.updateFoundRequest(item.requestId, "Accepted") {}
                                                    } else {
                                                        activityViewModel.updateBorrowRequest(item.requestId, "Accepted") {}
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Accept")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    if (item.type == "lost_found") {
                                                        activityViewModel.updateFoundRequest(item.requestId, "Rejected") {}
                                                    } else {
                                                        activityViewModel.updateBorrowRequest(item.requestId, "Rejected") {}
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    } else if (item.type == "borrow" && item.status == "Accepted" && item.itemId.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { onOpenChat(item) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open Chat")
                                        }
                                    }
                                }
                            }
                        }
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
fun MetricCard(
    value: String,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
