package com.example.campus_exchange_final.feature.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import com.example.campus_exchange_final.feature.borrow.BorrowRequestEntity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationEntity(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String = "",
    val message: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

data class ActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val type: String = "borrow", // "notification" or "borrow" or "lost_found"
    val status: String = "Pending",
    val requestId: String = "",
    val canAcceptReject: Boolean = false,
    val itemId: String = "",
    val ownerId: String = "",
    val requesterId: String = "",
    val isRead: Boolean = true
)

class ActivityViewModel : ViewModel() {
    var activities by mutableStateOf<List<ActivityItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchActivities()
    }

    fun fetchActivities() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    // 1. Notifications
                    val notifs = try {
                        SupabaseProvider.client.from("notifications")
                            .select(Columns.list("*")) {
                                filter {
                                    eq("user_id", userId)
                                }
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<NotificationEntity>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // 2. Borrow Requests (outgoing & incoming)
                    val borrowReqs = try {
                        SupabaseProvider.client.from("borrow_requests")
                            .select(Columns.list("*")) {
                                filter {
                                    or {
                                        eq("owner_id", userId)
                                        eq("requester_id", userId)
                                    }
                                }
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<BorrowRequestEntity>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // 3. User's Listed Items (from Lend Item)
                    val listedItems = try {
                        SupabaseProvider.client.from("items")
                            .select(Columns.list("*")) {
                                filter {
                                    eq("user_id", userId)
                                }
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<com.example.campus_exchange_final.feature.home.ItemListing>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // 4. User's Submitted Lost & Found Reports
                    val lostFoundReports = try {
                        SupabaseProvider.client.from("lost_found_reports")
                            .select(Columns.list("*")) {
                                filter {
                                    eq("user_id", userId)
                                }
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<com.example.campus_exchange_final.feature.lostfound.LostFoundItem>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // 5. Found Requests / Claims (incoming & outgoing)
                    val foundReqs = try {
                        SupabaseProvider.client.from("found_requests")
                            .select(Columns.list("*")) {
                                filter {
                                    or {
                                        eq("owner_id", userId)
                                        eq("requester_id", userId)
                                    }
                                }
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<com.example.campus_exchange_final.feature.lostfound.FoundRequestPayload>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    fun formatIsoTimestamp(rawTimestamp: String?): String {
                        if (rawTimestamp.isNullOrBlank()) return "Recent"
                        return try {
                            val clean = rawTimestamp.substringBefore(".").substringBefore("+").substringBefore("Z")
                            if (clean.contains("T")) {
                                val parts = clean.split("T")
                                val datePart = parts[0]
                                val timePart = parts.getOrNull(1)?.take(5) ?: ""
                                if (timePart.isNotBlank()) "$datePart $timePart" else datePart
                            } else {
                                clean.take(16)
                            }
                        } catch (_: Exception) {
                            rawTimestamp.take(16).replace("T", " ")
                        }
                    }

                    val notificationItems = notifs.map { notif ->
                        ActivityItem(
                            id = notif.id,
                            title = notif.title,
                            subtitle = notif.message,
                            timeAgo = formatIsoTimestamp(notif.createdAt),
                            type = "notification",
                            status = if (notif.isRead) "Read" else "Unread",
                            requestId = "",
                            canAcceptReject = false,
                            isRead = notif.isRead
                        )
                    }

                    val borrowItems = borrowReqs.mapIndexed { index, req ->
                        val isOwner = req.ownerId == userId
                        
                        val itemTitle = try {
                            val item = SupabaseProvider.client.from("items")
                                .select(Columns.list("*")) {
                                    filter { eq("id", req.itemId) }
                                }
                                .decodeSingleOrNull<com.example.campus_exchange_final.feature.home.ItemListing>()
                            item?.title
                        } catch (_: Exception) { null }

                        val otherUserId = if (isOwner) req.requesterId else req.ownerId
                        val otherProfile = try {
                            SupabaseProvider.client.from("public_profiles")
                                .select(Columns.list("*")) {
                                    filter { eq("id", otherUserId) }
                                }
                                .decodeSingleOrNull<com.example.campus_exchange_final.feature.profile.UserProfile>()
                        } catch (_: Exception) { null }

                        val otherName = otherProfile?.full_name ?: if (isOwner) "Requester" else "Owner"
                        val displayTitle = if (isOwner) {
                            "Incoming Borrow: ${itemTitle ?: "Item"}"
                        } else {
                            "Borrow Request: ${itemTitle ?: "Item"}"
                        }

                        val displaySubtitle = if (isOwner) {
                            "From $otherName • Status: ${req.status}"
                        } else {
                            "To $otherName • Status: ${req.status} • Duration: ${req.duration}"
                        }

                        ActivityItem(
                            id = req.id.ifBlank { "borrow_$index" },
                            title = displayTitle,
                            subtitle = if (req.message.isNotBlank()) "${req.message} ($displaySubtitle)" else displaySubtitle,
                            timeAgo = formatIsoTimestamp(req.createdAt),
                            type = "borrow",
                            status = req.status,
                            requestId = req.id,
                            canAcceptReject = isOwner && req.status == "Pending",
                            itemId = req.itemId,
                            ownerId = req.ownerId,
                            requesterId = req.requesterId
                        )
                    }

                    val listedActivityItems = listedItems.mapIndexed { index, item ->
                        ActivityItem(
                            id = item.id.ifBlank { "listed_$index" },
                            title = "Listed: ${item.title}",
                            subtitle = "Lending Type: ${item.lendingType} • Price: ${item.price} • ${item.location}",
                            timeAgo = "Listed Item",
                            type = "item_listed",
                            status = "Active",
                            requestId = "",
                            canAcceptReject = false,
                            itemId = item.id,
                            ownerId = item.userId,
                            requesterId = ""
                        )
                    }

                    val lostFoundReportItems = lostFoundReports.mapIndexed { index, report ->
                        val reportTypeLabel = if (report.isLost) "Reported Lost" else "Reported Found"
                        ActivityItem(
                            id = report.id.ifBlank { "lf_report_$index" },
                            title = "$reportTypeLabel: ${report.title}",
                            subtitle = "Category: ${report.category} • Location: ${report.location} • Status: ${report.status}",
                            timeAgo = if (report.isLost) "Lost Item" else "Found Item",
                            type = if (report.isLost) "lost_report" else "found_report",
                            status = report.status,
                            requestId = "",
                            canAcceptReject = false,
                            itemId = report.id,
                            ownerId = report.userId,
                            requesterId = ""
                        )
                    }

                    val foundItems = foundReqs.mapIndexed { index, req ->
                        val isOwner = req.ownerId == userId
                        val otherUserId = if (isOwner) req.requesterId else req.ownerId
                        val otherProfile = try {
                            SupabaseProvider.client.from("public_profiles")
                                .select(Columns.list("*")) {
                                    filter { eq("id", otherUserId) }
                                }
                                .decodeSingleOrNull<com.example.campus_exchange_final.feature.profile.UserProfile>()
                        } catch (_: Exception) { null }

                        val reportTitle = try {
                            val r = SupabaseProvider.client.from("lost_found_reports")
                                .select(Columns.list("*")) {
                                    filter { eq("id", req.reportId) }
                                }
                                .decodeSingleOrNull<com.example.campus_exchange_final.feature.lostfound.LostFoundItem>()
                            r?.title
                        } catch (_: Exception) { null }

                        val otherName = otherProfile?.full_name ?: if (isOwner) "Finder" else "Owner"
                        val displayTitle = if (isOwner) {
                            "Found Claim from $otherName: ${reportTitle ?: "Item"}"
                        } else {
                            "Claim Submitted to $otherName: ${reportTitle ?: "Item"}"
                        }
                        val displaySubtitle = "Status: ${req.status}" + if (req.message.isNotBlank()) " • \"${req.message}\"" else ""

                        ActivityItem(
                            id = req.id.ifBlank { "found_$index" },
                            title = displayTitle,
                            subtitle = displaySubtitle,
                            timeAgo = "Lost & Found",
                            type = "lost_found",
                            status = req.status,
                            requestId = req.id,
                            canAcceptReject = isOwner && req.status == "Pending",
                            itemId = req.reportId,
                            ownerId = req.ownerId,
                            requesterId = req.requesterId
                        )
                    }

                    // Combine and deduplicate
                    val combined = (notificationItems + borrowItems + listedActivityItems + lostFoundReportItems + foundItems)
                        .distinctBy { it.id }

                    withContext(Dispatchers.Main) {
                        activities = combined
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "User not logged in."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to load activity."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateBorrowRequest(requestId: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                SupabaseProvider.client.from("borrow_requests")
                    .update(mapOf("status" to newStatus)) {
                        filter {
                            eq("id", requestId)
                        }
                    }
                fetchActivities()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to update request."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateFoundRequest(requestId: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                SupabaseProvider.client.from("found_requests")
                    .update(mapOf("status" to newStatus)) {
                        filter {
                            eq("id", requestId)
                        }
                    }
                fetchActivities()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to update found request."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseProvider.client.from("notifications")
                    .update(mapOf("is_read" to true)) {
                        filter {
                            eq("id", notificationId)
                        }
                    }
                fetchActivities()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
