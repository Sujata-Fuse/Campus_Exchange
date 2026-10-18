package com.example.campus_exchange_final.feature.borrow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
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
import com.example.campus_exchange_final.core.ui.CampusButton
import com.example.campus_exchange_final.core.ui.CampusTextField
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.feature.home.ItemListing
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealFound
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

// Screen 12: Item Details
@Composable
fun ItemDetailsScreen(
    item: ItemListing,
    onRequestToBorrowClick: () -> Unit,
    onChatWithOwnerClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Image Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(PurpleSurface),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = BrandPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Owner Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BrandPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Priya Sharma",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Computer Science • 3rd Year",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lightly used MacBook Air 13\" M1. Perfect condition, clean display, includes original charger. Available for weekend borrowing.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleSurface)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Condition: Good",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealSurface)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Location: Hostel 1",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TealFound,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                CampusButton(
                    text = "Request to Borrow",
                    onClick = onRequestToBorrowClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = onChatWithOwnerClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandPurple
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandPurple)
                ) {
                    Text(
                        text = "Chat with Owner",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

// Screen 13: Request to Borrow Form
@Composable
fun RequestToBorrowScreen(
    item: ItemListing,
    onSendRequestClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    borrowViewModel: BorrowViewModel = remember { BorrowViewModel() }
) {
    var fromDate by remember { mutableStateOf("16 Nov 2024") }
    var toDate by remember { mutableStateOf("18 Nov 2024") }
    var message by remember { mutableStateOf("") }

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
            CampusTopBar(title = "Request to Borrow", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(20.dp))

            // Item summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.title.take(1),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = BrandPurple,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = item.price,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPurple,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CampusTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = "From Date",
                leadingIcon = Icons.Default.CalendarToday
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = "To Date",
                leadingIcon = Icons.Default.CalendarToday
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(
                value = message,
                onValueChange = { message = it },
                label = "Message (optional)",
                placeholder = "Hi, I would like to borrow this for my project presentation."
            )

            if (borrowViewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = borrowViewModel.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            CampusButton(
                text = if (borrowViewModel.isLoading) "Sending Request..." else "Send Request",
                onClick = {
                    borrowViewModel.submitBorrowRequest(
                        itemId = item.id,
                        ownerId = item.userId,
                        message = message,
                        duration = "$fromDate to $toDate"
                    ) {
                        onSendRequestClick()
                    }
                }
            )
        }
    }
}
