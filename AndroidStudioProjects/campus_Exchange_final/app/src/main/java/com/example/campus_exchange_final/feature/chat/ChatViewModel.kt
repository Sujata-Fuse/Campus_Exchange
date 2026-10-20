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
                    val result = SupabaseProvider.client.from("chats")
                        .select(Columns.list("*"))
                        .decodeList<ChatConversation>()
                    conversations = result
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
                        "text" to text
                    )
                    SupabaseProvider.client.from("messages").insert(payload)
                    fetchMessages(chatId)
                    onsuccess()
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to send message."
            }
        }
    }
}
