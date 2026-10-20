package com.example.campus_exchange_final.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val full_name: String,
    val course: String? = "Computer Science • 3rd Year",
    val phone: String? = "",
    val bio: String? = "",
    val avatar_url: String? = ""
)

class ProfileViewModel : ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var reportsCount by mutableStateOf(0)
        private set

    var borrowsCount by mutableStateOf(0)
        private set

    var claimsCount by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val userId = try {
                    SupabaseProvider.client.auth.currentUserOrNull()?.id
                } catch (_: Throwable) {
                    null
                }
                if (userId != null) {
                    val result = SupabaseProvider.client.from("profiles")
                        .select(Columns.list("*")) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingleOrNull<UserProfile>()

                    val userReports = try {
                        SupabaseProvider.client.from("lost_found_reports")
                            .select(Columns.list("id")) {
                                filter { eq("user_id", userId) }
                            }
                            .decodeList<Map<String, String>>()
                    } catch (_: Exception) { emptyList() }

                    val userBorrows = try {
                        SupabaseProvider.client.from("borrow_requests")
                            .select(Columns.list("id")) {
                                filter { eq("requester_id", userId) }
                            }
                            .decodeList<Map<String, String>>()
                    } catch (_: Exception) { emptyList() }

                    val userClaims = try {
                        SupabaseProvider.client.from("found_requests")
                            .select(Columns.list("id")) {
                                filter { eq("requester_id", userId) }
                            }
                            .decodeList<Map<String, String>>()
                    } catch (_: Exception) { emptyList() }

                    withContext(Dispatchers.Main) {
                        reportsCount = userReports.size
                        borrowsCount = userBorrows.size
                        claimsCount = userClaims.size

                        if (result != null) {
                            profile = result
                        } else {
                            val userEmail = try {
                                SupabaseProvider.client.auth.currentUserOrNull()?.email ?: "Student"
                            } catch (_: Throwable) {
                                "Student"
                            }
                            val metaName = try {
                                SupabaseProvider.client.auth.currentUserOrNull()?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")
                            } catch (_: Throwable) {
                                null
                            }
                            val defaultName = if (!metaName.isNullOrBlank() && metaName != "null") metaName else userEmail.substringBefore("@")

                            val newProfile = UserProfile(
                                id = userId,
                                full_name = defaultName,
                                course = "Computer Science • 3rd Year",
                                phone = "",
                                bio = ""
                            )
                            try {
                                SupabaseProvider.client.from("profiles").upsert(newProfile)
                            } catch (_: Throwable) {}
                            profile = newProfile
                        }
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.localizedMessage ?: "Failed to load profile."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateProfile(fullName: String, course: String, phone: String, bio: String, onsuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val userId = try {
                    SupabaseProvider.client.auth.currentUserOrNull()?.id
                } catch (_: Throwable) {
                    null
                }
                if (userId != null) {
                    val updated = UserProfile(
                        id = userId,
                        full_name = fullName,
                        course = course,
                        phone = phone,
                        bio = bio,
                        avatar_url = profile?.avatar_url ?: ""
                    )
                    SupabaseProvider.client.from("profiles").update(updated) {
                        filter {
                            eq("id", userId)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        profile = updated
                        successMessage = "Profile updated successfully!"
                        onsuccess()
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.localizedMessage ?: "Failed to update profile."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
