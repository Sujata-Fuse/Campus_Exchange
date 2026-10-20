package com.example.campus_exchange_final.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.campus_exchange_final.core.SupabaseProvider
import com.example.campus_exchange_final.core.ui.NavTab
import com.example.campus_exchange_final.core.ui.SuccessConfirmationScreen
import com.example.campus_exchange_final.feature.activity.ActivityScreen
import com.example.campus_exchange_final.feature.activity.NotificationsScreen
import com.example.campus_exchange_final.feature.auth.EmailVerificationScreen
import com.example.campus_exchange_final.feature.auth.ForgotPasswordScreen
import com.example.campus_exchange_final.feature.auth.LoginScreen
import com.example.campus_exchange_final.feature.auth.OnboardingScreen
import com.example.campus_exchange_final.feature.auth.RegisterScreen
import com.example.campus_exchange_final.feature.auth.SplashScreen
import com.example.campus_exchange_final.feature.borrow.ItemDetailsScreen
import com.example.campus_exchange_final.feature.borrow.LendItemScreen
import com.example.campus_exchange_final.feature.borrow.RequestToBorrowScreen
import com.example.campus_exchange_final.feature.chat.ChatConversation
import com.example.campus_exchange_final.feature.chat.ChatConversationScreen
import com.example.campus_exchange_final.feature.chat.ChatEntity
import com.example.campus_exchange_final.feature.chat.ChatInsertPayload
import com.example.campus_exchange_final.feature.chat.ChatListScreen
import com.example.campus_exchange_final.feature.home.HomeScreen
import com.example.campus_exchange_final.feature.home.ItemListing
import com.example.campus_exchange_final.feature.lostfound.LostFoundDashboardScreen
import com.example.campus_exchange_final.feature.lostfound.LostFoundDetailsScreen
import com.example.campus_exchange_final.feature.lostfound.LostFoundItem
import com.example.campus_exchange_final.feature.lostfound.ReportItemScreen
import com.example.campus_exchange_final.feature.profile.AboutScreen
import com.example.campus_exchange_final.feature.profile.AccountSecurityScreen
import com.example.campus_exchange_final.feature.profile.AccountSummaryScreen
import com.example.campus_exchange_final.feature.profile.EditProfileScreen
import com.example.campus_exchange_final.feature.profile.HelpFaqScreen
import com.example.campus_exchange_final.feature.profile.ProfileScreen
import com.example.campus_exchange_final.feature.profile.SafetyReportingScreen
import com.example.campus_exchange_final.feature.settings.PrivacyScreen
import com.example.campus_exchange_final.feature.settings.SettingsScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import com.example.campus_exchange_final.feature.chat.ChatWorkflowType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ScreenRoute {
    SPLASH,
    ONBOARDING_1,
    LOGIN,
    REGISTER,
    EMAIL_VERIFICATION,
    FORGOT_PASSWORD,
    HOME,
    ITEM_DETAILS,
    REQUEST_TO_BORROW,
    LOST_FOUND,
    LOST_FOUND_DETAILS,
    REPORT_LOST_ITEM,
    REPORT_FOUND_ITEM,
    ACTIVITY,
    PROFILE,
    SUCCESS_CONFIRMATION,
    CHAT_LIST,
    CHAT_CONVERSATION,
    EDIT_PROFILE,
    ACCOUNT_SUMMARY,
    SAFETY_REPORTING,
    SETTINGS,
    PRIVACY,
    HELP_FAQ,
    ABOUT,
    LEND_ITEM,
    NOTIFICATIONS
}

@Composable
fun CampusExchangeApp(modifier: Modifier = Modifier) {
    var screenStack by remember { mutableStateOf(listOf(ScreenRoute.SPLASH)) }
    val currentScreen = screenStack.lastOrNull() ?: ScreenRoute.SPLASH

    var selectedItem by remember { mutableStateOf<ItemListing?>(null) }
    var selectedLostItem by remember { mutableStateOf<LostFoundItem?>(null) }
    var activeConversation by remember { mutableStateOf<ChatConversation?>(null) }
    var activeTab by remember { mutableStateOf(NavTab.HOME) }

    fun navigateTo(route: ScreenRoute) {
        screenStack = screenStack + route
    }

    fun popBackStack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        }
    }

    // System Back Handler support
    BackHandler(enabled = screenStack.size > 1) {
        popBackStack()
    }

    when (currentScreen) {
        ScreenRoute.SPLASH -> {
            SplashScreen()
            var isLoggedIn by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                isLoggedIn = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        SupabaseProvider.client.auth.currentUserOrNull() != null
                    } catch (_: Throwable) {
                        false
                    }
                }
                kotlinx.coroutines.delay(1500)
                if (screenStack.lastOrNull() == ScreenRoute.SPLASH) {
                    if (isLoggedIn) {
                        screenStack = listOf(ScreenRoute.HOME)
                    } else {
                        screenStack = listOf(ScreenRoute.ONBOARDING_1)
                    }
                }
            }

            androidx.compose.foundation.layout.Box(
                modifier = modifier.fillMaxSize().clickable {
                    if (screenStack.lastOrNull() == ScreenRoute.SPLASH) {
                        if (isLoggedIn) {
                            screenStack = listOf(ScreenRoute.HOME)
                        } else {
                            screenStack = listOf(ScreenRoute.ONBOARDING_1)
                        }
                    }
                }
            )
        }

        ScreenRoute.ONBOARDING_1 -> {
            OnboardingScreen(
                onGetStartedClick = { navigateTo(ScreenRoute.LOGIN) }
            )
        }

        ScreenRoute.LOGIN -> {
            LoginScreen(
                onLoginClick = { screenStack = listOf(ScreenRoute.HOME) },
                onSignUpClick = { navigateTo(ScreenRoute.REGISTER) },
                onForgotPasswordClick = { navigateTo(ScreenRoute.FORGOT_PASSWORD) }
            )
        }

        ScreenRoute.REGISTER -> {
            RegisterScreen(
                onSignUpClick = { navigateTo(ScreenRoute.EMAIL_VERIFICATION) },
                onLoginClick = { popBackStack() },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.EMAIL_VERIFICATION -> {
            EmailVerificationScreen(
                onVerifySuccess = { screenStack = listOf(ScreenRoute.HOME) },
                onBackToLogin = { popBackStack() }
            )
        }

        ScreenRoute.FORGOT_PASSWORD -> {
            ForgotPasswordScreen(
                onSendResetLink = { navigateTo(ScreenRoute.SUCCESS_CONFIRMATION) },
                onBackToLogin = { popBackStack() }
            )
        }

        ScreenRoute.HOME -> {
            activeTab = NavTab.HOME
            HomeScreen(
                onItemClick = { item ->
                    selectedItem = item
                    navigateTo(ScreenRoute.ITEM_DETAILS)
                },
                onBorrowBannerClick = { navigateTo(ScreenRoute.LEND_ITEM) },
                onLostFoundBannerClick = { navigateTo(ScreenRoute.LOST_FOUND) },
                onNotificationClick = { navigateTo(ScreenRoute.NOTIFICATIONS) },
                onChatListClick = { navigateTo(ScreenRoute.CHAT_LIST) },
                selectedTab = activeTab,
                onTabSelected = { tab ->
                    activeTab = tab
                    when (tab) {
                        NavTab.HOME -> {}
                        NavTab.BORROW -> {}
                        NavTab.LOST_FOUND -> navigateTo(ScreenRoute.LOST_FOUND)
                        NavTab.ACTIVITY -> navigateTo(ScreenRoute.ACTIVITY)
                        NavTab.PROFILE -> navigateTo(ScreenRoute.PROFILE)
                    }
                },
                modifier = modifier
            )
        }

        ScreenRoute.ITEM_DETAILS -> {
            selectedItem?.let { item ->
                ItemDetailsScreen(
                    item = item,
                    onRequestToBorrowClick = { navigateTo(ScreenRoute.REQUEST_TO_BORROW) },
                    onChatWithOwnerClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
                            val ownerId = item.userId.ifBlank { currentUserId }
                            if (currentUserId.isBlank() || ownerId.isBlank() || currentUserId == ownerId) return@launch
                            try {
                                val existing = SupabaseProvider.client.from("chats")
                                    .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                        filter {
                                            eq("item_id", item.id)
                                        }
                                    }
                                    .decodeList<ChatEntity>()

                                val chat = existing.firstOrNull { 
                                    (it.participantOne == ownerId && it.participantTwo == currentUserId) ||
                                    (it.participantOne == currentUserId && it.participantTwo == ownerId)
                                }

                                val targetChat = if (chat != null) {
                                    chat
                                } else {
                                    val newPayload = ChatInsertPayload(
                                        itemId = item.id,
                                        participantOne = ownerId,
                                        participantTwo = currentUserId,
                                        workflowType = "BORROW_LEND",
                                        lastMessage = "Chat started for ${item.title}"
                                    )
                                    val inserted = SupabaseProvider.client.from("chats").insert(newPayload) {
                                        select()
                                    }.decodeSingle<ChatEntity>()
                                    inserted
                                }

                                val ownerProfile = try {
                                    SupabaseProvider.client.from("public_profiles")
                                        .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                            filter { eq("id", ownerId) }
                                        }
                                        .decodeSingleOrNull<com.example.campus_exchange_final.feature.profile.UserProfile>()
                                } catch (_: Exception) { null }

                                val finalChat = ChatConversation(
                                    id = targetChat.id,
                                    participantName = ownerProfile?.full_name ?: "Item Owner",
                                    participantRole = ownerProfile?.course ?: "Student",
                                    relatedItemTitle = item.title,
                                    relatedItemSubtitle = "Borrow/Lend",
                                    workflowType = ChatWorkflowType.BORROW_LEND,
                                    lastMessage = targetChat.lastMessage,
                                    timestamp = targetChat.createdAt,
                                    itemId = item.id,
                                    participantOne = targetChat.participantOne,
                                    participantTwo = targetChat.participantTwo
                                )

                                activeConversation = finalChat
                                navigateTo(ScreenRoute.CHAT_CONVERSATION)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onOpenChatWithUser = { otherUserId, otherUserName ->
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
                            val ownerId = item.userId.ifBlank { currentUserId }
                            if (currentUserId.isBlank() || otherUserId.isBlank() || currentUserId == otherUserId) return@launch
                            try {
                                val existing = SupabaseProvider.client.from("chats")
                                    .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                        filter {
                                            eq("item_id", item.id)
                                        }
                                    }
                                    .decodeList<ChatEntity>()

                                val chat = existing.firstOrNull {
                                    (it.participantOne == ownerId && it.participantTwo == otherUserId) ||
                                    (it.participantOne == otherUserId && it.participantTwo == ownerId)
                                }

                                val targetChat = if (chat != null) {
                                    chat
                                } else {
                                    val newPayload = ChatInsertPayload(
                                        itemId = item.id,
                                        participantOne = ownerId,
                                        participantTwo = otherUserId,
                                        workflowType = "BORROW_LEND",
                                        lastMessage = "Chat started for ${item.title}"
                                    )
                                    val inserted = SupabaseProvider.client.from("chats").insert(newPayload) {
                                        select()
                                    }.decodeSingle<ChatEntity>()
                                    inserted
                                }

                                activeConversation = ChatConversation(
                                    id = targetChat.id,
                                    participantName = otherUserName,
                                    participantRole = "Borrower",
                                    relatedItemTitle = item.title,
                                    relatedItemSubtitle = "Borrow/Lend",
                                    workflowType = ChatWorkflowType.BORROW_LEND,
                                    lastMessage = targetChat.lastMessage,
                                    timestamp = targetChat.createdAt,
                                    itemId = item.id,
                                    participantOne = targetChat.participantOne,
                                    participantTwo = targetChat.participantTwo
                                )
                                navigateTo(ScreenRoute.CHAT_CONVERSATION)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onBackClick = { popBackStack() }
                )
            }
        }

        ScreenRoute.REQUEST_TO_BORROW -> {
            selectedItem?.let { item ->
                RequestToBorrowScreen(
                    item = item,
                    onSendRequestClick = { navigateTo(ScreenRoute.SUCCESS_CONFIRMATION) },
                    onBackClick = { popBackStack() }
                )
            }
        }

        ScreenRoute.LOST_FOUND -> {
            LostFoundDashboardScreen(
                onReportLostClick = { navigateTo(ScreenRoute.REPORT_LOST_ITEM) },
                onReportFoundClick = { navigateTo(ScreenRoute.REPORT_FOUND_ITEM) },
                onItemClick = { item ->
                    selectedLostItem = item
                    navigateTo(ScreenRoute.LOST_FOUND_DETAILS)
                },
                selectedTab = activeTab,
                onTabSelected = { tab ->
                    activeTab = tab
                    when (tab) {
                        NavTab.HOME -> screenStack = listOf(ScreenRoute.HOME)
                        NavTab.BORROW -> {}
                        NavTab.LOST_FOUND -> {}
                        NavTab.ACTIVITY -> navigateTo(ScreenRoute.ACTIVITY)
                        NavTab.PROFILE -> navigateTo(ScreenRoute.PROFILE)
                    }
                }
            )
        }

        ScreenRoute.LOST_FOUND_DETAILS -> {
            selectedLostItem?.let { item ->
                LostFoundDetailsScreen(
                    item = item,
                    onBackClick = { popBackStack() },
                    onOpenChat = { otherUserId, otherUserName ->
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
                            val ownerId = item.userId.ifBlank { currentUserId }
                            if (currentUserId.isBlank() || otherUserId.isBlank() || currentUserId == otherUserId) return@launch
                            try {
                                val existing = SupabaseProvider.client.from("chats")
                                    .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                        filter {
                                            eq("lost_found_id", item.id)
                                        }
                                    }
                                    .decodeList<ChatEntity>()

                                val chat = existing.firstOrNull {
                                    (it.participantOne == ownerId && it.participantTwo == otherUserId) ||
                                    (it.participantOne == otherUserId && it.participantTwo == ownerId)
                                }

                                val targetChat = if (chat != null) {
                                    chat
                                } else {
                                    val newPayload = ChatInsertPayload(
                                        lostFoundId = item.id,
                                        participantOne = ownerId,
                                        participantTwo = otherUserId,
                                        workflowType = "LOST_FOUND",
                                        lastMessage = "Chat started for ${item.title}"
                                    )
                                    val inserted = SupabaseProvider.client.from("chats").insert(newPayload) {
                                        select()
                                    }.decodeSingle<ChatEntity>()
                                    inserted
                                }

                                activeConversation = ChatConversation(
                                    id = targetChat.id,
                                    participantName = otherUserName,
                                    participantRole = "Finder",
                                    relatedItemTitle = item.title,
                                    relatedItemSubtitle = "Lost & Found",
                                    workflowType = ChatWorkflowType.LOST_FOUND,
                                    lastMessage = targetChat.lastMessage,
                                    timestamp = targetChat.createdAt,
                                    itemId = null,
                                    participantOne = targetChat.participantOne,
                                    participantTwo = targetChat.participantTwo
                                )
                                navigateTo(ScreenRoute.CHAT_CONVERSATION)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
        }

        ScreenRoute.REPORT_LOST_ITEM -> {
            ReportItemScreen(
                isLost = true,
                onSubmitSuccess = { navigateTo(ScreenRoute.SUCCESS_CONFIRMATION) },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.REPORT_FOUND_ITEM -> {
            ReportItemScreen(
                isLost = false,
                onSubmitSuccess = { navigateTo(ScreenRoute.SUCCESS_CONFIRMATION) },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.ACTIVITY -> {
            ActivityScreen(
                onOpenChat = { activityItem ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
                        val ownerId = activityItem.ownerId
                        val requesterId = activityItem.requesterId
                        val entityId = activityItem.itemId
                        val isLostFound = activityItem.type == "lost_found"
                        if (currentUserId.isBlank() || ownerId.isBlank() || requesterId.isBlank() || entityId.isBlank()) return@launch

                        try {
                            val existing = SupabaseProvider.client.from("chats")
                                .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                    filter {
                                        if (isLostFound) {
                                            eq("lost_found_id", entityId)
                                        } else {
                                            eq("item_id", entityId)
                                        }
                                    }
                                }
                                .decodeList<ChatEntity>()

                            val chat = existing.firstOrNull {
                                (it.participantOne == ownerId && it.participantTwo == requesterId) ||
                                (it.participantOne == requesterId && it.participantTwo == ownerId)
                            }

                            val targetChat = if (chat != null) {
                                chat
                            } else {
                                val newPayload = if (isLostFound) {
                                    ChatInsertPayload(
                                        lostFoundId = entityId,
                                        participantOne = ownerId,
                                        participantTwo = requesterId,
                                        workflowType = "LOST_FOUND",
                                        lastMessage = "Found claim accepted. Chat opened."
                                    )
                                } else {
                                    ChatInsertPayload(
                                        itemId = entityId,
                                        participantOne = ownerId,
                                        participantTwo = requesterId,
                                        workflowType = "BORROW_LEND",
                                        lastMessage = "Borrow request accepted. Chat opened."
                                    )
                                }
                                SupabaseProvider.client.from("chats").insert(newPayload) {
                                    select()
                                }.decodeSingle<ChatEntity>()
                            }

                            val otherUserId = if (currentUserId == ownerId) requesterId else ownerId
                            val otherProfile = try {
                                SupabaseProvider.client.from("public_profiles")
                                    .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                        filter { eq("id", otherUserId) }
                                    }
                                    .decodeSingleOrNull<com.example.campus_exchange_final.feature.profile.UserProfile>()
                            } catch (_: Exception) { null }

                            val titleResolved = if (isLostFound) {
                                try {
                                    val report = SupabaseProvider.client.from("lost_found_reports")
                                        .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                            filter { eq("id", entityId) }
                                        }
                                        .decodeSingleOrNull<com.example.campus_exchange_final.feature.lostfound.LostFoundItem>()
                                    report?.title
                                } catch (_: Exception) { null }
                            } else {
                                try {
                                    val item = SupabaseProvider.client.from("items")
                                        .select(io.github.jan.supabase.postgrest.query.Columns.list("*")) {
                                            filter { eq("id", entityId) }
                                        }
                                        .decodeSingleOrNull<ItemListing>()
                                    item?.title
                                } catch (_: Exception) { null }
                            }

                            activeConversation = ChatConversation(
                                id = targetChat.id,
                                participantName = otherProfile?.full_name ?: if (currentUserId == ownerId) "Finder/Requester" else "Owner",
                                participantRole = otherProfile?.course ?: "Student",
                                relatedItemTitle = titleResolved ?: "Campus Exchange Item",
                                relatedItemSubtitle = if (isLostFound) "Lost & Found" else "Borrow/Lend",
                                workflowType = if (isLostFound) ChatWorkflowType.LOST_FOUND else ChatWorkflowType.BORROW_LEND,
                                lastMessage = targetChat.lastMessage,
                                timestamp = targetChat.createdAt,
                                itemId = if (!isLostFound) entityId else null,
                                participantOne = targetChat.participantOne,
                                participantTwo = targetChat.participantTwo
                            )
                            navigateTo(ScreenRoute.CHAT_CONVERSATION)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                selectedTab = activeTab,
                onTabSelected = { tab ->
                    activeTab = tab
                    when (tab) {
                        NavTab.HOME -> screenStack = listOf(ScreenRoute.HOME)
                        NavTab.BORROW -> {}
                        NavTab.LOST_FOUND -> navigateTo(ScreenRoute.LOST_FOUND)
                        NavTab.ACTIVITY -> {}
                        NavTab.PROFILE -> navigateTo(ScreenRoute.PROFILE)
                    }
                }
            )
        }

        ScreenRoute.PROFILE -> {
            ProfileScreen(
                onEditProfileClick = { navigateTo(ScreenRoute.EDIT_PROFILE) },
                onAccountSummaryClick = { navigateTo(ScreenRoute.ACCOUNT_SUMMARY) },
                onSafetyClick = { navigateTo(ScreenRoute.SAFETY_REPORTING) },
                onSettingsClick = { navigateTo(ScreenRoute.SETTINGS) },
                selectedTab = activeTab,
                onTabSelected = { tab ->
                    activeTab = tab
                    when (tab) {
                        NavTab.HOME -> screenStack = listOf(ScreenRoute.HOME)
                        NavTab.BORROW -> {}
                        NavTab.LOST_FOUND -> navigateTo(ScreenRoute.LOST_FOUND)
                        NavTab.ACTIVITY -> navigateTo(ScreenRoute.ACTIVITY)
                        NavTab.PROFILE -> {}
                    }
                },
                modifier = modifier
            )
        }

        ScreenRoute.EDIT_PROFILE -> {
            EditProfileScreen(
                onSaveClick = { popBackStack() },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.ACCOUNT_SUMMARY -> {
            AccountSummaryScreen(
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.SAFETY_REPORTING -> {
            SafetyReportingScreen(
                onReportClick = { navigateTo(ScreenRoute.REPORT_LOST_ITEM) },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.SETTINGS -> {
            SettingsScreen(
                onAccountSecurityClick = { navigateTo(ScreenRoute.ACCOUNT_SUMMARY) },
                onPrivacyClick = { navigateTo(ScreenRoute.PRIVACY) },
                onHelpFaqClick = { navigateTo(ScreenRoute.HELP_FAQ) },
                onAboutClick = { navigateTo(ScreenRoute.ABOUT) },
                onLogoutClick = { screenStack = listOf(ScreenRoute.LOGIN) },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.PRIVACY -> {
            PrivacyScreen(
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.HELP_FAQ -> {
            HelpFaqScreen(
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.ABOUT -> {
            AboutScreen(
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.LEND_ITEM -> {
            LendItemScreen(
                onListSuccess = { navigateTo(ScreenRoute.SUCCESS_CONFIRMATION) },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.NOTIFICATIONS -> {
            NotificationsScreen(
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.SUCCESS_CONFIRMATION -> {
            SuccessConfirmationScreen(
                onButtonClick = { screenStack = listOf(ScreenRoute.HOME) }
            )
        }

        ScreenRoute.CHAT_LIST -> {
            ChatListScreen(
                onConversationClick = { conv ->
                    activeConversation = conv
                    navigateTo(ScreenRoute.CHAT_CONVERSATION)
                },
                onBackClick = { popBackStack() }
            )
        }

        ScreenRoute.CHAT_CONVERSATION -> {
            activeConversation?.let { conv ->
                ChatConversationScreen(
                    conversation = conv,
                    onBackClick = { popBackStack() }
                )
            }
        }
    }
}
