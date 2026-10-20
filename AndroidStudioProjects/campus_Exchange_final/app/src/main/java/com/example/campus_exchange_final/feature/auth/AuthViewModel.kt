package com.example.campus_exchange_final.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun signUp(email: String, password: String, fullName: String = "", onsuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseProvider.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    if (fullName.isNotBlank()) {
                        data = buildJsonObject {
                            put("full_name", fullName)
                        }
                    }
                }
                successMessage = "Registration successful! Please check your email for confirmation."
                onsuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Registration failed."
            } finally {
                isLoading = false
            }
        }
    }

    fun signIn(email: String, password: String, onsuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                onsuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.localizedMessage ?: "Login failed."
                errorMessage = when {
                    msg.contains("not confirmed", ignoreCase = true) -> {
                        "Email not confirmed. Please check your inbox for the confirmation link."
                    }
                    msg.contains("invalid_credentials", ignoreCase = true) || msg.contains("invalid login", ignoreCase = true) -> {
                        "Invalid email or password. Please try again."
                    }
                    msg.contains("Socket closed", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) || msg.contains("ConnectException", ignoreCase = true) -> {
                        "Network request timed out. Please check your internet connection and try again."
                    }
                    else -> msg
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword(email: String, onsuccess: () -> Unit) {
        if (email.isBlank()) {
            errorMessage = "Please enter your email address."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseProvider.client.auth.resetPasswordForEmail(email)
                successMessage = "Password reset link sent to your email."
                onsuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Failed to send password reset link."
            } finally {
                isLoading = false
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseProvider.client.auth.signOut()
            } catch (_: Exception) {}
            onSignedOut()
        }
    }

    fun checkSession(): Boolean {
        return try {
            SupabaseProvider.client.auth.currentUserOrNull() != null
        } catch (_: Exception) {
            false
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
