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
import com.example.campus_exchange_final.feature.chat.ChatListScreen
import com.example.campus_exchange_final.feature.chat.sampleConversations
import com.example.campus_exchange_final.feature.home.HomeScreen
import com.example.campus_exchange_final.feature.home.ItemListing
import com.example.campus_exchange_final.feature.home.sampleListings
import com.example.campus_exchange_final.feature.lostfound.LostFoundDashboardScreen
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
    LEND_ITEM
}

@Composable
fun CampusExchangeApp(modifier: Modifier = Modifier) {
    var screenStack by remember { mutableStateOf(listOf(ScreenRoute.SPLASH)) }
    val currentScreen = screenStack.lastOrNull() ?: ScreenRoute.SPLASH

    var selectedItem by remember { mutableStateOf<ItemListing?>(sampleListings.first()) }
    var activeConversation by remember { mutableStateOf<ChatConversation?>(sampleConversations.first()) }
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
                    onChatWithOwnerClick = { navigateTo(ScreenRoute.CHAT_CONVERSATION) },
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
                onItemClick = { /* item click */ },
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
