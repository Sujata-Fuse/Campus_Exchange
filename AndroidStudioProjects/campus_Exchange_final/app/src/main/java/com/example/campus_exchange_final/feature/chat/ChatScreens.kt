package com.example.campus_exchange_final.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

            val displayConversations = chatViewModel.conversations

            if (displayConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active chats",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
                    )
                }
            } else {
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PurpleSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.participantName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = BrandPurple,
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
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = conversation.relatedItemTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp
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
    
    androidx.compose.runtime.LaunchedEffect(conversation.id) {
        if (conversation.id.isNotBlank()) {
            chatViewModel.fetchMessages(conversation.id)
        }
    }

    val displayMessages = chatViewModel.messages

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
                                text = "${conversation.participantRole} • ${conversation.relatedItemTitle}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Messages List
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
                            text = "Type a message...",
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
                .border(
                    width = if (message.isMe) 0.dp else 1.dp,
                    color = if (message.isMe) Color.Transparent else BorderLight,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMe) 16.dp else 4.dp,
                        bottomEnd = if (message.isMe) 4.dp else 16.dp
                    )
                )
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
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )
    }
}
