package com.example.campus_exchange_final.feature.lostfound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val status: String = "Active"
)

@Serializable
data class FoundRequestPayload(
    val id: String = "",
    @SerialName("report_id") val reportId: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("owner_id") val ownerId: String,
    val status: String = "Pending",
    val message: String = ""
)

class LostFoundViewModel : ViewModel() {
    var reports by mutableStateOf<List<LostFoundItem>>(emptyList())
        private set

    var foundRequests by mutableStateOf<List<FoundRequestPayload>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchReports()
        fetchFoundRequests()
    }

    fun fetchReports() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val result = SupabaseProvider.client.from("lost_found_reports")
                    .select(Columns.list("*"))
                    .decodeList<LostFoundItem>()
                withContext(Dispatchers.Main) {
                    reports = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to load lost & found reports."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun fetchFoundRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val result = SupabaseProvider.client.from("found_requests")
                        .select(Columns.list("*"))
                        .decodeList<FoundRequestPayload>()
                    withContext(Dispatchers.Main) {
                        foundRequests = result.filter { it.requesterId == userId || it.ownerId == userId }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun submitReport(
        title: String,
        isLost: Boolean,
        category: String,
        location: String,
        description: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                successMessage = null
            }
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    if (title.isBlank() || category.isBlank() || location.isBlank()) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Please fill in all required fields."
                        }
                        return@launch
                    }
                    val report = LostFoundItem(
                        userId = userId,
                        title = title,
                        isLost = isLost,
                        category = category,
                        location = location,
                        description = description,
                        status = "Active"
                    )
                    SupabaseProvider.client.from("lost_found_reports").insert(report)
                    fetchReports()
                    withContext(Dispatchers.Main) {
                        successMessage = "Report submitted successfully!"
                        onsuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Must be logged in to report an item."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to submit report."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun submitFoundRequest(
        reportId: String,
        ownerId: String,
        message: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                successMessage = null
            }
            try {
                val requesterId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (requesterId != null) {
                    if (requesterId == ownerId) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "You cannot submit a found request for your own lost report."
                        }
                        return@launch
                    }
                    val request = FoundRequestPayload(
                        reportId = reportId,
                        requesterId = requesterId,
                        ownerId = ownerId,
                        message = message,
                        status = "Pending"
                    )
                    SupabaseProvider.client.from("found_requests").insert(request)
                    fetchFoundRequests()
                    withContext(Dispatchers.Main) {
                        successMessage = "Found request sent successfully!"
                        onsuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Must be logged in to send a found request."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to submit found request."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun deleteReport(
        reportId: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "User not authenticated. Please log in first."
                    }
                    return@launch
                }

                // Delete the report from lost_found_reports table
                SupabaseProvider.client.from("lost_found_reports").delete {
                    filter {
                        eq("id", reportId)
                        eq("user_id", userId)
                    }
                }

                fetchReports()
                withContext(Dispatchers.Main) {
                    successMessage = "Report deleted successfully!"
                    onsuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to delete report from database."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateFoundRequestStatus(
        requestId: String,
        newStatus: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseProvider.client.from("found_requests")
                    .update(mapOf("status" to newStatus)) {
                        filter {
                            eq("id", requestId)
                        }
                    }
                fetchFoundRequests()
                withContext(Dispatchers.Main) {
                    onsuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.localizedMessage ?: "Failed to update status."
                }
            }
        }
    }
}
