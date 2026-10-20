package com.example.campus_exchange_final.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    var conversations by mutableStateOf<List<ChatConversation>>(emptyList())
        private set

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchConversations()
    }

    fun fetchConversations() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val entities = SupabaseProvider.client.from("chats")
                        .select(Columns.list("*"))
                        .decodeList<ChatEntity>()

                    val enrichedList = entities.map { entity ->
                        val otherUserId = if (entity.participantOne == userId) entity.participantTwo else entity.participantOne
                        val otherProfile = if (otherUserId.isNotBlank()) {
                            try {
                                SupabaseProvider.client.from("public_profiles")
                                    .select(Columns.list("*")) {
                                        filter { eq("id", otherUserId) }
                                    }
                                    .decodeSingleOrNull<com.example.campus_exchange_final.feature.profile.UserProfile>()
                            } catch (_: Exception) { null }
                        } else null

                        val itemTitle = if (!entity.itemId.isNullOrBlank()) {
                            try {
                                val item = SupabaseProvider.client.from("items")
                                    .select(Columns.list("*")) {
                                        filter { eq("id", entity.itemId) }
                                    }
                                    .decodeSingleOrNull<com.example.campus_exchange_final.feature.home.ItemListing>()
                                item?.title
                            } catch (_: Exception) { null }
                        } else if (!entity.lostFoundId.isNullOrBlank()) {
                            try {
                                val lfItem = SupabaseProvider.client.from("lost_found_reports")
                                    .select(Columns.list("*")) {
                                        filter { eq("id", entity.lostFoundId) }
                                    }
                                    .decodeSingleOrNull<com.example.campus_exchange_final.feature.lostfound.LostFoundItem>()
                                lfItem?.title
                            } catch (_: Exception) { null }
                        } else null

                        val workflowTypeEnum = try {
                            ChatWorkflowType.valueOf(entity.workflowType)
                        } catch (_: Exception) {
                            ChatWorkflowType.BORROW_LEND
                        }

                        ChatConversation(
                            id = entity.id,
                            participantName = otherProfile?.full_name ?: "Campus Student",
                            participantRole = otherProfile?.course ?: "Student",
                            relatedItemTitle = itemTitle ?: "Campus Exchange Item",
                            relatedItemSubtitle = if (workflowTypeEnum == ChatWorkflowType.BORROW_LEND) "Borrow/Lend" else "Lost & Found",
                            workflowType = workflowTypeEnum,
                            lastMessage = entity.lastMessage,
                            timestamp = entity.createdAt,
                            itemId = entity.itemId,
                            participantOne = entity.participantOne,
                            participantTwo = entity.participantTwo
                        )
                    }
                    conversations = enrichedList
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load chats."
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
                val result = SupabaseProvider.client.from("messages")
                    .select(Columns.list("*")) {
                        filter {
                            eq("chat_id", chatId)
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    }
                    .decodeList<ChatMessage>()
                messages = result.map { it.copy(isMe = it.senderName == currentUserId) }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load messages."
            } finally {
                isLoading = false
            }
        }
    }

    fun sendMessage(chatId: String, text: String, onsuccess: () -> Unit) {
        if (text.isBlank()) return
        viewModelScope.launch {
            errorMessage = null
            try {
                val senderId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (senderId != null) {
                    val payload = mapOf(
                        "chat_id" to chatId,
                        "sender_id" to senderId,
                        "text" to text.trim()
                    )
                    SupabaseProvider.client.from("messages").insert(payload)
                    try {
                        SupabaseProvider.client.from("chats").update(mapOf("last_message" to text.trim())) {
                            filter { eq("id", chatId) }
                        }
                    } catch (_: Exception) { }
                    fetchMessages(chatId)
                    onsuccess()
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to send message."
            }
        }
    }
}
