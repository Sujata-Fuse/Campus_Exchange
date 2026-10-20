package com.example.campus_exchange_final.feature.borrow

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

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

@Serializable
data class BorrowRequestEntity(
    val id: String = "",
    @SerialName("item_id") val itemId: String = "",
    @SerialName("requester_id") val requesterId: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val status: String = "Pending",
    val message: String = "",
    val duration: String = "7 Days",
    @SerialName("created_at") val createdAt: String = ""
)

class BorrowViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    suspend fun uploadItemImage(context: Context, imageUri: Uri, userId: String): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalArgumentException("Cannot open image input stream.")
        val bytes = inputStream.use { it.readBytes() }
        val extension = when (context.contentResolver.getType(imageUri)) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val filename = "${UUID.randomUUID()}.$extension"
        val storagePath = "$userId/$filename"
        val bucket = SupabaseProvider.client.storage.from("campus-images")
        bucket.upload(storagePath, bytes) {
            upsert = false
        }
        return bucket.publicUrl(storagePath)
    }

    fun listItem(
        title: String,
        category: String,
        location: String,
        lendingType: String,
        price: String,
        duration: String,
        condition: String = "",
        imageUri: Uri? = null,
        context: Context? = null,
        description: String = "",
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
                if (userId.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "User not authenticated. Please log in first."
                        isLoading = false
                    }
                    return@launch
                }

                val uploadedImageUrl: String = if (imageUri != null && context != null) {
                    try {
                        uploadItemImage(context, imageUri, userId)
                    } catch (uploadEx: Throwable) {
                        uploadEx.printStackTrace()
                        withContext(Dispatchers.Main) {
                            errorMessage = "Image upload failed: ${uploadEx.localizedMessage ?: "Unknown storage error"}"
                            isLoading = false
                        }
                        return@launch
                    }
                } else {
                    ""
                }

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

                val durationParts = mutableListOf<String>()
                if (duration.isNotBlank()) durationParts.add(duration)
                if (condition.isNotBlank()) durationParts.add("Condition: $condition")
                if (description.isNotBlank()) durationParts.add("Note: $description")
                val formattedDuration = durationParts.joinToString(" • ")

                val payload = ItemInsertPayload(
                    userId = userId,
                    title = title,
                    category = category,
                    location = location,
                    lendingType = lendingType,
                    price = formattedPrice,
                    duration = formattedDuration,
                    imageUrl = uploadedImageUrl
                )

                SupabaseProvider.client.from("items").insert(payload)
                withContext(Dispatchers.Main) {
                    successMessage = "Item successfully listed!"
                    onsuccess()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${t.localizedMessage ?: t.javaClass.simpleName}"
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
                if (requesterId.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "User not authenticated."
                        isLoading = false
                    }
                    return@launch
                }

                // Prevent users from requesting their own items
                if (ownerId.isNotBlank() && requesterId == ownerId) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "You cannot request to borrow your own item."
                        isLoading = false
                    }
                    return@launch
                }

                // Check request eligibility
                val existingRequests = try {
                    SupabaseProvider.client.from("borrow_requests")
                        .select(Columns.list("*")) {
                            filter {
                                eq("item_id", itemId)
                            }
                        }
                        .decodeList<BorrowRequestEntity>()
                } catch (_: Exception) {
                    emptyList()
                }

                val activeRequest = existingRequests.firstOrNull { it.status == "Pending" || it.status == "Accepted" }
                if (activeRequest != null) {
                    withContext(Dispatchers.Main) {
                        if (activeRequest.requesterId == requesterId) {
                            errorMessage = "You already have a pending or accepted request for this item."
                        } else if (activeRequest.status == "Accepted") {
                            errorMessage = "This item is currently borrowed and unavailable."
                        } else {
                            errorMessage = "A borrow request is already pending for this item."
                        }
                        isLoading = false
                    }
                    return@launch
                }

                val request = BorrowRequestPayload(
                    itemId = itemId,
                    requesterId = requesterId,
                    ownerId = ownerId,
                    message = message,
                    duration = duration.ifBlank { "7 Days" },
                    status = "Pending"
                )
                SupabaseProvider.client.from("borrow_requests").insert(request)
                withContext(Dispatchers.Main) {
                    onsuccess()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${t.localizedMessage ?: t.javaClass.simpleName}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
}
