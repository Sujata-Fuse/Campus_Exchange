package com.example.campus_exchange_final.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.feature.auth.BackgroundLogoWatermark
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BorderLight
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TealFound
import com.example.campus_exchange_final.ui.theme.TealSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

val sampleConversations = listOf(
    ChatConversation(
        id = "conv_1",
        participantName = "Rahul Verma",
        participantRole = "Item Owner",
        relatedItemTitle = "MacBook Air 13\"",
        relatedItemSubtitle = "₹8,000 • Borrow & Lend",
        workflowType = ChatWorkflowType.BORROW_LEND,
        lastMessage = "Is the laptop still available for this weekend?",
        timestamp = "10:42 AM",
        unreadCount = 2
    ),
    ChatConversation(
        id = "conv_2",
        participantName = "Ananya Patel",
        participantRole = "Finder",
        relatedItemTitle = "Leather Wallet",
        relatedItemSubtitle = "Lost Item Report • Main Canteen",
        workflowType = ChatWorkflowType.LOST_FOUND,
        lastMessage = "I found a brown wallet near table 4. Does it have your ID inside?",
        timestamp = "Yesterday",
        unreadCount = 0
    )
)

val sampleMessages = mapOf(
    "conv_1" to listOf(
        ChatMessage("m1", "conv_1", "Rahul Verma", "Hi Priya, saw your request for the MacBook.", "10:35 AM", false),
        ChatMessage("m2", "conv_1", "Priya Sharma", "Yes! I need it for a presentation on Friday.", "10:38 AM", true),
        ChatMessage("m3", "conv_1", "Rahul Verma", "Is the laptop still available for this weekend?", "10:42 AM", false)
    ),
    "conv_2" to listOf(
        ChatMessage("m4", "conv_2", "Ananya Patel", "I found a brown wallet near table 4. Does it have your ID inside?", "Yesterday", false)
    )
)

// 1. Chat List Screen
@Composable
fun ChatListScreen(
    onConversationClick: (ChatConversation) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = remember { ChatViewModel() }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        BackgroundLogoWatermark(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            CampusTopBar(title = "Campus Chats", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(12.dp))

            val displayConversations = if (chatViewModel.conversations.isNotEmpty()) chatViewModel.conversations else sampleConversations

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayConversations) { conv ->
                    ChatConversationCard(
                        conversation = conv,
                        onClick = { onConversationClick(conv) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Participant Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (conversation.workflowType == ChatWorkflowType.BORROW_LEND) PurpleSurface else TealSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.participantName.take(1),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (conversation.workflowType == ChatWorkflowType.BORROW_LEND) BrandPurple else TealFound,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.participantName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = conversation.timestamp,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Related Item Context Badge
                Text(
                    text = "📦 ${conversation.relatedItemTitle}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (conversation.workflowType == ChatWorkflowType.BORROW_LEND) BrandPurple else TealFound
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = TextSecondary
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

// 2. Chat Conversation Screen
@Composable
fun ChatConversationScreen(
    conversation: ChatConversation,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = remember { ChatViewModel() }
) {
    var messageInput by remember { mutableStateOf("") }
    val displayMessages = if (chatViewModel.messages.isNotEmpty()) chatViewModel.messages else (sampleMessages[conversation.id] ?: emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header Bar with Context
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = conversation.participantName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = conversation.participantRole,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Related Item Context Banner inside header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (conversation.workflowType == ChatWorkflowType.BORROW_LEND) PurpleSurface else TealSurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Related Item: ",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            )
                            Text(
                                text = conversation.relatedItemTitle,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (conversation.workflowType == ChatWorkflowType.BORROW_LEND) BrandPurple else TealFound
                                )
                            )
                        }
                    }
                }
            }

            // Message Bubbles List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                items(displayMessages) { msg ->
                    MessageBubble(message = msg)
                }
            }

            // Message Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            text = "Type a message regarding item verification...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppBackground,
                        unfocusedContainerColor = AppBackground,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = BorderLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            val textToSend = messageInput
                            messageInput = ""
                            chatViewModel.sendMessage(conversation.id, textToSend) {}
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandPurple)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMe) 16.dp else 4.dp,
                        bottomEnd = if (message.isMe) 4.dp else 16.dp
                    )
                )
                .background(if (message.isMe) BrandPurple else Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (message.isMe) Color.White else TextPrimary,
                    fontSize = 14.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                color = TextSecondary
            )
        )
    }
}
