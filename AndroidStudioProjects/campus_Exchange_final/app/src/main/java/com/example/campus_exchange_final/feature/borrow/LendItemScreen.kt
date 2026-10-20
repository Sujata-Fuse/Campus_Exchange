package com.example.campus_exchange_final.feature.borrow

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campus_exchange_final.core.ui.CampusButton
import com.example.campus_exchange_final.core.ui.CampusTextField
import com.example.campus_exchange_final.core.ui.CampusTopBar
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BorderLight
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.PurpleSurface
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

@Composable
fun LendItemScreen(
    onListSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    borrowViewModel: BorrowViewModel = remember { BorrowViewModel() }
) {
    val context = LocalContext.current
    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Books") }
    var customCategory by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Good") }
    var location by remember { mutableStateOf("") }
    
    // Lending Options: 0 = Lend Free, 1 = Sell / Fixed Price, 2 = Rent on Duration
    var lendingType by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf("1 Week") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Books", "Electronics", "Fashion", "Furniture", "Notes", "Sports", "Other")
    val durations = listOf("1 Day", "3 Days", "1 Week", "2 Weeks", "1 Month")

    var categoryExpanded by remember { mutableStateOf(false) }
    var durationExpanded by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CampusTopBar(title = "List New Item to Lend", onBackClick = onBackClick)
            }

            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Item Photo Picker / Preview
                Text(
                    text = "Item Photo",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PurpleSurface)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Item Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = BrandPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap to Add Item Photo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BrandPurple,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name
                CampusTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = "Item Name *",
                    placeholder = "e.g. Scientific Calculator / Textbook"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection Dropdown (Fully Clickable)
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand Category")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BrandPurple,
                            unfocusedBorderColor = BorderLight,
                            disabledBorderColor = BorderLight,
                            disabledTextColor = TextPrimary,
                            disabledTrailingIconColor = TextPrimary
                        ),
                        enabled = false
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCategory == "Other") {
                    Spacer(modifier = Modifier.height(12.dp))
                    CampusTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = "Specify Custom Category *",
                        placeholder = "Enter category name..."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lending Type Option: Free, Fixed Price (Sell), Rent (Duration)
                Text(
                    text = "Lending Option *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Lend Free", "Fixed Price", "Rent (Duration)").forEachIndexed { index, label ->
                        val isSelected = lendingType == index
                        OutlinedButton(
                            onClick = { lendingType = index },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) BrandPurple else Color.White,
                                contentColor = if (isSelected) Color.White else TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) BrandPurple else BorderLight
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Conditional Duration Field (Only appears when Rent on Duration is selected)
                if (lendingType == 2) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rental Duration *",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { durationExpanded = true }
                    ) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { durationExpanded = !durationExpanded }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand Duration")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = BrandPurple,
                                unfocusedBorderColor = BorderLight,
                                disabledBorderColor = BorderLight,
                                disabledTextColor = TextPrimary,
                                disabledTrailingIconColor = TextPrimary
                            ),
                            enabled = false
                        )
                        DropdownMenu(
                            expanded = durationExpanded,
                            onDismissRequest = { durationExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            durations.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        duration = d
                                        durationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Price Field (Only if not Lend Free)
                if (lendingType != 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Price (₹) *",
                        placeholder = "e.g. 150",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = "Condition",
                    placeholder = "Like New, Good, Fair..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Pickup Location / Hostel *",
                    placeholder = "Hostel 1, Library..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Provide brief details..."
                )

                if (errorMessage != null || borrowViewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: borrowViewModel.errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fixed Sticky Bottom Action Bar with Submit Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    CampusButton(
                        text = if (borrowViewModel.isLoading) "Listing Item..." else "List Item",
                        enabled = !borrowViewModel.isLoading,
                        onClick = {
                            val finalCategory = if (selectedCategory == "Other") customCategory else selectedCategory
                            val typeLabel = when (lendingType) {
                                1 -> "Fixed Price"
                                2 -> "Rent on Duration"
                                else -> "Free"
                            }

                            if (itemName.isBlank()) {
                                errorMessage = "Please enter an item name."
                                android.widget.Toast.makeText(context, "Please enter an item name.", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (selectedCategory == "Other" && customCategory.isBlank()) {
                                errorMessage = "Please specify a custom category."
                                android.widget.Toast.makeText(context, "Please specify a custom category.", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (lendingType != 0 && price.isBlank()) {
                                errorMessage = "Please enter a price."
                                android.widget.Toast.makeText(context, "Please enter a price.", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (location.isBlank()) {
                                errorMessage = "Please specify a pickup location."
                                android.widget.Toast.makeText(context, "Please specify a pickup location.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                errorMessage = null
                                borrowViewModel.listItem(
                                    title = itemName,
                                    category = finalCategory,
                                    location = location,
                                    lendingType = typeLabel,
                                    price = price,
                                    duration = if (lendingType == 2) duration else "",
                                    condition = condition,
                                    imageUri = imageUri,
                                    context = context,
                                    description = description
                                ) {
                                    android.widget.Toast.makeText(context, "Item listed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    onListSuccess()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
