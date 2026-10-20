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
    val type: String = "borrow", // "notification" or "borrow"
    val status: String = "Pending",
    val requestId: String = "",
    val canAcceptReject: Boolean = false
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
                    val notifs = try {
                        SupabaseProvider.client.from("notifications")
                            .select(Columns.list("*")) {
                                filter {
                                    eq("user_id", userId)
                                }
                            }
                            .decodeList<NotificationEntity>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val borrowReqs = try {
                        SupabaseProvider.client.from("borrow_requests")
                            .select(Columns.list("*")) {
                                filter {
                                    or {
                                        eq("owner_id", userId)
                                        eq("requester_id", userId)
                                    }
                                }
                            }
                            .decodeList<BorrowRequestEntity>()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val notificationItems = notifs.map { notif ->
                        ActivityItem(
                            id = notif.id,
                            title = notif.title,
                            subtitle = notif.message,
                            timeAgo = "Recent",
                            type = "notification",
                            status = if (notif.isRead) "Read" else "Unread",
                            requestId = "",
                            canAcceptReject = false
                        )
                    }

                    val borrowItems = borrowReqs.mapIndexed { index, req ->
                        val isOwner = req.ownerId == userId
                        val title = if (isOwner) "Borrow Request Received: ${req.status}" else "Borrow Request Sent: ${req.status}"
                        ActivityItem(
                            id = req.id.ifBlank { "borrow_$index" },
                            title = title,
                            subtitle = req.message.ifBlank { "Duration: ${req.duration}" },
                            timeAgo = req.duration,
                            type = "borrow",
                            status = req.status,
                            requestId = req.id,
                            canAcceptReject = isOwner && req.status == "Pending"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        activities = notificationItems + borrowItems
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
}
