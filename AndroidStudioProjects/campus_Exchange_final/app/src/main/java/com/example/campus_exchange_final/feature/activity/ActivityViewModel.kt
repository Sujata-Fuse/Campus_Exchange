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
                        e.printStackTrace()
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
                                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                            }
                            .decodeList<BorrowRequestEntity>()
                    } catch (e: Exception) {
                        emptyList()
                    }

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

                    val notificationItems = notifs.map { notif ->
                        val formattedTime = notif.createdAt.take(16).replace("T", " ").ifBlank { "Recent" }
                        ActivityItem(
                            id = notif.id,
                            title = notif.title,
                            subtitle = notif.message,
                            timeAgo = formattedTime,
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
                            "Borrow Request: ${itemTitle ?: "Item"}"
                        } else {
                            "Request to ${otherName}: ${itemTitle ?: "Item"}"
                        }

                        val displaySubtitle = if (isOwner) {
                            "From $otherName • Status: ${req.status}"
                        } else {
                            "Status: ${req.status} • Duration: ${req.duration}"
                        }

                        ActivityItem(
                            id = req.id.ifBlank { "borrow_$index" },
                            title = displayTitle,
                            subtitle = if (req.message.isNotBlank()) "${req.message} ($displaySubtitle)" else displaySubtitle,
                            timeAgo = req.duration,
                            type = "borrow",
                            status = req.status,
                            requestId = req.id,
                            canAcceptReject = isOwner && req.status == "Pending",
                            itemId = req.itemId,
                            ownerId = req.ownerId,
                            requesterId = req.requesterId
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

                        val otherName = otherProfile?.full_name ?: if (isOwner) "Finder" else "Owner"
                        val displayTitle = if (isOwner) "Found Alert from $otherName" else "Found Claim sent to $otherName"
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

                    withContext(Dispatchers.Main) {
                        activities = notificationItems + borrowItems + foundItems
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
