package com.example.campus_exchange_final.feature.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ChatWorkflowType {
    BORROW_LEND,
    LOST_FOUND
}

@Serializable
data class ChatInsertPayload(
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("lost_found_id") val lostFoundId: String? = null,
    @SerialName("participant_one") val participantOne: String,
    @SerialName("participant_two") val participantTwo: String,
    @SerialName("workflow_type") val workflowType: String = "BORROW_LEND",
    @SerialName("last_message") val lastMessage: String = ""
)

@Serializable
data class ChatEntity(
    val id: String = "",
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("lost_found_id") val lostFoundId: String? = null,
    @SerialName("participant_one") val participantOne: String = "",
    @SerialName("participant_two") val participantTwo: String = "",
    @SerialName("workflow_type") val workflowType: String = "BORROW_LEND",
    @SerialName("last_message") val lastMessage: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class ChatConversation(
    val id: String = "",
    val participantName: String = "Student",
    val participantRole: String = "Item Owner",
    val relatedItemTitle: String = "Campus Exchange Item",
    val relatedItemSubtitle: String = "Borrow",
    val workflowType: ChatWorkflowType = ChatWorkflowType.BORROW_LEND,
    @SerialName("last_message") val lastMessage: String = "",
    @SerialName("created_at") val timestamp: String = "",
    val unreadCount: Int = 0,
    val isAuthorized: Boolean = true,
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("participant_one") val participantOne: String = "",
    @SerialName("participant_two") val participantTwo: String = ""
)

@Serializable
data class ChatMessage(
    val id: String = "",
    @SerialName("chat_id") val conversationId: String = "",
    @SerialName("sender_id") val senderName: String = "Student",
    val text: String = "",
    @SerialName("created_at") val timestamp: String = "",
    val isMe: Boolean = true
)

