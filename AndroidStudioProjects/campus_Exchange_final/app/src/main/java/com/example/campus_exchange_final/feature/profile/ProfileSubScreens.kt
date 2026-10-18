package com.example.campus_exchange_final.feature.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.core.ui.CampusButton
import com.example.campus_exchange_final.core.ui.CampusTextField
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

// Screen 36: Edit Profile
@Composable
fun EditProfileScreen(
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = remember { ProfileViewModel() }
) {
    val currentProfile = profileViewModel.profile
    var name by remember(currentProfile) { mutableStateOf(currentProfile?.full_name ?: "") }
    var course by remember(currentProfile) { mutableStateOf(currentProfile?.course ?: "Computer Science • 3rd Year") }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile?.phone ?: "") }
    var bio by remember(currentProfile) { mutableStateOf(currentProfile?.bio ?: "") }

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
            CampusTopBar(title = "Edit Profile", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            CampusTextField(value = name, onValueChange = { name = it }, label = "Full Name", leadingIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(16.dp))
            CampusTextField(value = course, onValueChange = { course = it }, label = "Course & Year")
            Spacer(modifier = Modifier.height(16.dp))
            CampusTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number (Optional)",
                placeholder = "e.g. +91 9876543210",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampusTextField(value = bio, onValueChange = { bio = it }, label = "About Me")

            if (profileViewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profileViewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            CampusButton(
                text = if (profileViewModel.isLoading) "Saving..." else "Save Changes",
                onClick = {
                    profileViewModel.updateProfile(name, course, phone, bio) {
                        onSaveClick()
                    }
                }
            )
        }
    }
}

// Screen 37: My Account Summary
@Composable
fun AccountSummaryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            CampusTopBar(title = "My Account Summary", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SummaryRow("College Email", "priya.sharma@college.edu")
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryRow("Member Since", "August 2023")
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryRow("Total Borrow Requests", "5")
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryRow("Total Claims Submitted", "5")
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryRow("Trust Score", "4.9 / 5.0 ⭐")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
    }
}

// Screen 38 & 39: Safety & Reporting
@Composable
fun SafetyReportingScreen(
    onReportClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            CampusTopBar(title = "Safety & Reporting", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "A safer campus starts with you.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPurple)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Report suspicious users or fraudulent listings instantly. Our campus security team reviews reports within 24 hours.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CampusButton(text = "Report a User or Listing", onClick = onReportClick)
        }
    }
}

// Screen 41: Account & Security
@Composable
fun AccountSecurityScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            CampusTopBar(title = "Account & Security", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Change Password", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Update your account password regularly.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Two-Factor Authentication", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Enabled via college OTP verification.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                }
            }
        }
    }
}

// Screen 43: Help & FAQ
@Composable
fun HelpFaqScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            CampusTopBar(title = "Help & FAQ", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(16.dp))

            FaqItem("How do I borrow an item?", "Browse available items on the home screen, tap an item, and click 'Request to Borrow' or 'Chat with Owner'.")
            Spacer(modifier = Modifier.height(12.dp))
            FaqItem("How does Lost & Found work?", "Report lost or found items with location details. Our smart match algorithm suggests matches instantly.")
            Spacer(modifier = Modifier.height(12.dp))
            FaqItem("Is Campus Exchange free?", "Yes! Campus Exchange is built by students, for students, to facilitate campus sharing.")
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPurple))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = answer, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        }
    }
}

// Screen 44: About Campus Exchange
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CampusTopBar(title = "About Campus Exchange", onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BrandPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "CE", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Campus Exchange v1.0.0", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A campus community platform for borrowing, lending and finding lost items. Built for students, by students.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
