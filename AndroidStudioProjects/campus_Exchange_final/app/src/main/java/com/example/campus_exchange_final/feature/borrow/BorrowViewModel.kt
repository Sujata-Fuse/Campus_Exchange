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

    suspend fun uploadItemImage(context: Context, imageUri: Uri, userId: String): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Cannot open image input stream.")

        if (bytes.isEmpty()) {
            throw IllegalArgumentException("Selected image file is empty.")
        }

        // 15MB size limit safety check
        if (bytes.size > 15 * 1024 * 1024) {
            throw IllegalArgumentException("Selected image is too large (maximum size is 15MB).")
        }

        val mimeType = try {
            context.contentResolver.getType(imageUri)
        } catch (_: Exception) {
            null
        }

        val extension = when (mimeType?.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
        val filename = "${UUID.randomUUID()}.$extension"
        val storagePath = "$userId/$filename"
        val bucket = SupabaseProvider.client.storage.from("campus-images")
        bucket.upload(storagePath, bytes) {
            upsert = false
        }
        bucket.publicUrl(storagePath)
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
                        } else {
                            errorMessage = "This item already has an active borrow request."
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
                val messageText = t.localizedMessage ?: ""
                withContext(Dispatchers.Main) {
                    if (messageText.contains("duplicate", ignoreCase = true) ||
                        messageText.contains("unique", ignoreCase = true) ||
                        messageText.contains("23505")
                    ) {
                        errorMessage = "This item already has an active borrow request."
                    } else {
                        errorMessage = "Error: ${t.localizedMessage ?: t.javaClass.simpleName}"
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun deleteItem(
        itemId: String,
        imageUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (currentUserId.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        onError("User not authenticated.")
                    }
                    return@launch
                }

                // Check for Pending or Accepted borrow requests before deletion
                val activeRequests = try {
                    SupabaseProvider.client.from("borrow_requests")
                        .select(Columns.list("*")) {
                            filter {
                                eq("item_id", itemId)
                            }
                        }
                        .decodeList<BorrowRequestEntity>()
                        .filter { it.status == "Pending" || it.status == "Accepted" }
                } catch (_: Exception) {
                    emptyList()
                }

                if (activeRequests.isNotEmpty()) {
                    val hasAccepted = activeRequests.any { it.status == "Accepted" }
                    val msg = if (hasAccepted) {
                        "Cannot delete item: It has an accepted/ongoing borrow request. Please complete the return first."
                    } else {
                        "Cannot delete item: There is a pending borrow request on this item. Please accept or reject it first."
                    }
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        onError(msg)
                    }
                    return@launch
                }

                // Delete item row from Supabase items table (RLS enforces auth.uid() = user_id)
                SupabaseProvider.client.from("items").delete {
                    filter {
                        eq("id", itemId)
                        eq("user_id", currentUserId)
                    }
                }

                // Clean up item image from Supabase Storage bucket if applicable
                if (imageUrl.isNotBlank() && imageUrl.contains("campus-images")) {
                    try {
                        val path = imageUrl.substringAfter("campus-images/").substringBefore("?")
                        if (path.startsWith(currentUserId)) {
                            SupabaseProvider.client.storage.from("campus-images").delete(listOf(path))
                        }
                    } catch (storageEx: Throwable) {
                        // Storage cleanup failure does not fail the item deletion
                        storageEx.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    isLoading = false
                    onSuccess()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoading = false
                    val errorMsg = t.localizedMessage ?: "Failed to delete item."
                    onError(errorMsg)
                }
            }
        }
    }
}

