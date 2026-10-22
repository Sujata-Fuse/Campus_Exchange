package com.example.campus_exchange_final.feature.borrow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campus_exchange_final.core.SupabaseProvider
import com.example.campus_exchange_final.core.ui.CampusButton
import com.example.campus_exchange_final.core.ui.CampusTextField
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.feature.home.ItemListing
import com.example.campus_exchange_final.feature.home.formatItemDisplayPrice
import com.example.campus_exchange_final.feature.profile.UserProfile
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealFound
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EnrichedBorrowRequest(
    val entity: BorrowRequestEntity,
    val requesterName: String = "Student",
    val requesterCourse: String = "Student"
)

// Screen 12: Item Details
@Composable
fun ItemDetailsScreen(
    item: ItemListing,
    onRequestToBorrowClick: () -> Unit,
    onChatWithOwnerClick: () -> Unit,
    onOpenChatWithUser: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit = onBackClick,
    modifier: Modifier = Modifier,
    borrowViewModel: BorrowViewModel = remember { BorrowViewModel() }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var ownerName by remember { mutableStateOf("Loading owner...") }
    var ownerCourse by remember { mutableStateOf("Student") }
    var isOwner by remember { mutableStateOf(false) }
    var incomingRequests by remember { mutableStateOf<List<EnrichedBorrowRequest>>(emptyList()) }
    var userActiveRequest by remember { mutableStateOf<BorrowRequestEntity?>(null) }
    var isItemUnavailable by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.id, item.userId) {
        val currentUserId = try {
            SupabaseProvider.client.auth.currentUserOrNull()?.id
        } catch (_: Exception) {
            null
        }
        isOwner = currentUserId != null && currentUserId == item.userId

        if (isOwner) {
            try {
                val reqs = withContext(Dispatchers.IO) {
                    SupabaseProvider.client.from("borrow_requests")
                        .select(Columns.list("*")) {
                            filter { eq("item_id", item.id) }
                        }
                        .decodeList<BorrowRequestEntity>()
                }
                
                val enriched = withContext(Dispatchers.IO) {
                    reqs.map { req ->
                        val profile = try {
                            SupabaseProvider.client.from("public_profiles")
                                .select(Columns.list("*")) {
                                    filter { eq("id", req.requesterId) }
                                }
                                .decodeSingleOrNull<UserProfile>()
                        } catch (_: Exception) { null }

                        EnrichedBorrowRequest(
                            entity = req,
                            requesterName = profile?.full_name ?: "Campus Student",
                            requesterCourse = profile?.course ?: "Student"
                        )
                    }
                }
                incomingRequests = enriched
            } catch (_: Exception) {
                incomingRequests = emptyList()
            }
        } else if (currentUserId != null) {
            // For potential borrowers, check if user has an existing request or if item is already accepted/borrowed
            try {
                val reqs = withContext(Dispatchers.IO) {
                    SupabaseProvider.client.from("borrow_requests")
                        .select(Columns.list("*")) {
                            filter { eq("item_id", item.id) }
                        }
                        .decodeList<BorrowRequestEntity>()
                }
                userActiveRequest = reqs.firstOrNull { it.requesterId == currentUserId && (it.status == "Pending" || it.status == "Accepted") }
                val otherActiveRequest = reqs.firstOrNull { it.requesterId != currentUserId && (it.status == "Pending" || it.status == "Accepted") }
                isItemUnavailable = otherActiveRequest != null
            } catch (_: Exception) {
                userActiveRequest = null
                isItemUnavailable = false
            }
        }

        if (item.userId.isNotBlank()) {
            try {
                val profile = withContext(Dispatchers.IO) {
                    SupabaseProvider.client.from("public_profiles")
                        .select(Columns.list("*")) {
                            filter { eq("id", item.userId) }
                        }
                        .decodeSingleOrNull<UserProfile>()
                }
                if (profile != null) {
                    ownerName = profile.full_name
                    ownerCourse = profile.course ?: "Student"
                } else {
                    ownerName = "Campus Student"
                }
            } catch (_: Exception) {
                ownerName = "Campus Student"
                ownerCourse = "Student"
            }
        } else {
            ownerName = "Campus Student"
            ownerCourse = "Student"
        }
    }

    fun updateRequestStatus(requestId: String, status: String) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseProvider.client.from("borrow_requests")
                    .update(mapOf("status" to status)) {
                        filter { eq("id", requestId) }
                    }
                val reqs = SupabaseProvider.client.from("borrow_requests")
                    .select(Columns.list("*")) {
                        filter { eq("item_id", item.id) }
                    }
                    .decodeList<BorrowRequestEntity>()

                val enriched = reqs.map { req ->
                    val profile = try {
                        SupabaseProvider.client.from("public_profiles")
                            .select(Columns.list("*")) {
                                filter { eq("id", req.requesterId) }
                            }
                            .decodeSingleOrNull<UserProfile>()
                    } catch (_: Exception) { null }

                    EnrichedBorrowRequest(
                        entity = req,
                        requesterName = profile?.full_name ?: "Campus Student",
                        requesterCourse = profile?.course ?: "Student"
                    )
                }

                withContext(Dispatchers.Main) {
                    incomingRequests = enriched
                }
            } catch (_: Exception) {}
        }
    }

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

                if (isOwner) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(end = 16.dp, top = 8.dp)
                            .background(Color.White.copy(alpha = 0.85f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = BrandPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp)
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
                        text = formatItemDisplayPrice(item),
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
                                text = ownerName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = ownerCourse,
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
                    text = item.description.ifBlank { "No description provided for this item." },
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
                            text = "Condition: ${item.condition.ifBlank { "Good" }}",
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
                            text = "Location: ${item.location}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TealFound,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                val rentalDuration = item.duration.substringBefore(" •").trim()
                if (rentalDuration.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Rental Duration: $rentalDuration",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFE65100),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (isOwner) {
                    Text(
                        text = "Owner Management Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrandPurple
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (incomingRequests.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "No incoming borrow requests for this item yet.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                        ) {
                            incomingRequests.forEach { enriched ->
                                val req = enriched.entity
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = enriched.requesterName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = TextPrimary
                                                    )
                                                )
                                                Text(
                                                    text = enriched.requesterCourse,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = TextSecondary,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when (req.status) {
                                                            "Accepted" -> TealSurface
                                                            "Rejected" -> Color(0xFFFFEBEE)
                                                            else -> PurpleSurface
                                                        }
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = req.status,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (req.status) {
                                                            "Accepted" -> TealFound
                                                            "Rejected" -> MaterialTheme.colorScheme.error
                                                            else -> BrandPurple
                                                        }
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Requested Duration: ${req.duration}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = req.message.ifBlank { "No message provided." },
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                        )

                                        if (req.status == "Pending") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                            ) {
                                                androidx.compose.material3.Button(
                                                    onClick = {
                                                        updateRequestStatus(req.id, "Accepted")
                                                        incomingRequests.filter { it.entity.id != req.id && it.entity.status == "Pending" }.forEach { other ->
                                                            updateRequestStatus(other.entity.id, "Rejected")
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Accept")
                                                }
                                                androidx.compose.material3.OutlinedButton(
                                                    onClick = { updateRequestStatus(req.id, "Rejected") },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject")
                                                }
                                            }
                                        } else if (req.status == "Accepted") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            androidx.compose.material3.OutlinedButton(
                                                onClick = { onOpenChatWithUser(req.requesterId, enriched.requesterName) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Chat with Borrower")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    androidx.compose.material3.OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete This Listed Item",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                } else {
                    if (userActiveRequest != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (userActiveRequest?.status == "Accepted") TealSurface else PurpleSurface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (userActiveRequest?.status == "Accepted") "Borrow Request Accepted!" else "Request Submitted (Pending)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (userActiveRequest?.status == "Accepted") TealFound else BrandPurple
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (userActiveRequest?.status == "Accepted") 
                                        "The owner accepted your request. You can coordinate pickup via Chat." 
                                        else "You have an active pending request for this item.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else if (isItemUnavailable) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Item Currently Unavailable",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This item already has an active borrow request.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        CampusButton(
                            text = "Request to Borrow",
                            onClick = onRequestToBorrowClick
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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

        if (showDeleteConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    if (!borrowViewModel.isLoading) {
                        showDeleteConfirmDialog = false
                        deleteErrorMessage = null
                    }
                },
                title = {
                    Text(
                        text = "Delete Item Listing",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to delete this item? This action will permanently remove it from Campus Exchange.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        if (deleteErrorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = deleteErrorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            deleteErrorMessage = null
                            borrowViewModel.deleteItem(
                                itemId = item.id,
                                imageUrl = item.imageUrl,
                                onSuccess = {
                                    showDeleteConfirmDialog = false
                                    android.widget.Toast.makeText(context, "Item deleted successfully.", android.widget.Toast.LENGTH_SHORT).show()
                                    onDeleteSuccess()
                                },
                                onError = { msg ->
                                    deleteErrorMessage = msg
                                }
                            )
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !borrowViewModel.isLoading
                    ) {
                        if (borrowViewModel.isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deleting...")
                        } else {
                            Text("Delete")
                        }
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            deleteErrorMessage = null
                        },
                        enabled = !borrowViewModel.isLoading
                    ) {
                        Text("Cancel")
                    }
                }
            )
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
                .navigationBarsPadding()
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
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = item.title.take(1),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = BrandPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
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
                            text = formatItemDisplayPrice(item),
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

            val isRent = item.lendingType.contains("Rent", ignoreCase = true)
            val ownerDuration = item.duration.substringBefore(" •").trim()

            if (isRent && ownerDuration.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Owner's Rental Duration",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPurple
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ownerDuration,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }
                }
            } else {
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
            }

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
                enabled = !borrowViewModel.isLoading,
                onClick = {
                    val finalDuration = if (isRent && ownerDuration.isNotBlank()) ownerDuration else "$fromDate to $toDate"
                    borrowViewModel.submitBorrowRequest(
                        itemId = item.id,
                        ownerId = item.userId,
                        message = message,
                        duration = finalDuration
                    ) {
                        onSendRequestClick()
                    }
                }
            )
        }
    }
}
