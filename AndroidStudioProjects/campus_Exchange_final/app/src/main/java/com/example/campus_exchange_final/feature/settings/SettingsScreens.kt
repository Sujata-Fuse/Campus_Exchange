package com.example.campus_exchange_final.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.feature.auth.AuthViewModel
import com.example.campus_exchange_final.feature.auth.BackgroundLogoWatermark
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

// Screen 40: Settings
@Composable
fun SettingsScreen(
    onAccountSecurityClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onHelpFaqClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = remember { AuthViewModel() }
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CampusTopBar(title = "Settings", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            SettingRow(Icons.Default.Notifications, "Notifications", "Push & email alerts") { }
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Palette, "Appearance", "Theme & display") { }
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Language, "Language", "English (US)") { }
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Lock, "Account & Security", "Password & 2FA", onClick = onAccountSecurityClick)
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Visibility, "Privacy", "Visibility & location", onClick = onPrivacyClick)
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Devices, "Help & FAQ", "Guides & support", onClick = onHelpFaqClick)
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Info, "About Campus Exchange", "Version 1.0.0", onClick = onAboutClick)
            Spacer(modifier = Modifier.height(10.dp))
            SettingRow(Icons.Default.Lock, "Logout", "Sign out of your account", onClick = {
                authViewModel.signOut {
                    onLogoutClick()
                }
            })
        }
    }
}

// Screen 42: Privacy
@Composable
fun PrivacyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileVisible by remember { mutableStateOf(true) }
    var shareActivity by remember { mutableStateOf(true) }
    var locationAccess by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CampusTopBar(title = "Privacy", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            PrivacyToggleRow("Profile Visibility", "Allow other students to view your profile", profileVisible) { profileVisible = it }
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyToggleRow("Show My Activity", "Display recent borrows and reports", shareActivity) { shareActivity = it }
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyToggleRow("Allow Location Access", "Use precise location for Lost & Found", locationAccess) { locationAccess = it }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrandPurple
                )
            )
        }
    }
}
