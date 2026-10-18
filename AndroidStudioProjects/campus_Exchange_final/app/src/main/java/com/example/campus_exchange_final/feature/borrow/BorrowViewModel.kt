package com.example.campus_exchange_final.feature.borrow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemInsertPayload(
    @SerialName("user_id") val userId: String,
    val title: String,
    val price: String,
    val category: String,
    val location: String,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("lending_type") val lendingType: String = "Free",
    val duration: String = ""
)

@Serializable
data class BorrowRequestPayload(
    @SerialName("item_id") val itemId: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("owner_id") val ownerId: String,
    val status: String = "Pending",
    val message: String = "",
    val duration: String = "7 Days"
)

class BorrowViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun listItem(
        title: String,
        category: String,
        location: String,
        lendingType: String,
        price: String,
        duration: String,
        condition: String = "",
        imageUrl: String = "",
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
                    val formattedPrice = when (lendingType) {
                        "Fixed Price", "Rent on Duration" -> {
                            if (price.isBlank()) {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Please enter a price."
                                    isLoading = false
                                }
                                return@launch
                            }
                            if (price.startsWith("₹")) price else "₹$price"
                        }
                        else -> "Free"
                    }

                    val formattedDuration = when {
                        duration.isNotBlank() && condition.isNotBlank() -> "$duration • Condition: $condition"
                        duration.isNotBlank() -> duration
                        condition.isNotBlank() -> "Condition: $condition"
                        else -> ""
                    }

                    val payload = ItemInsertPayload(
                        userId = userId,
                        title = title,
                        category = category,
                        location = location,
                        lendingType = lendingType,
                        price = formattedPrice,
                        duration = formattedDuration,
                        imageUrl = imageUrl
                    )

                    SupabaseProvider.client.from("items").insert(payload)
                    withContext(Dispatchers.Main) {
                        successMessage = "Item successfully listed!"
                        onsuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Must be logged in to list an item."
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.localizedMessage ?: "Failed to list item in database."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun submitBorrowRequest(
        itemId: String,
        ownerId: String,
        message: String,
        duration: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val requesterId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (requesterId != null) {
                    val request = BorrowRequestPayload(
                        itemId = itemId,
                        requesterId = requesterId,
                        ownerId = if (ownerId.isNotBlank()) ownerId else requesterId,
                        message = message,
                        duration = duration
                    )
                    SupabaseProvider.client.from("borrow_requests").insert(request)
                    withContext(Dispatchers.Main) {
                        onsuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Must be logged in to send a request."
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.localizedMessage ?: "Failed to submit borrow request."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
}
