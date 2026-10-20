package com.example.campus_exchange_final.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import com.example.campus_exchange_final.feature.profile.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    var listings by mutableStateOf<List<ItemListing>>(emptyList())
        private set

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchListings()
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val profile = SupabaseProvider.client.from("profiles")
                        .select(Columns.list("*")) {
                            filter { eq("id", userId) }
                        }
                        .decodeSingleOrNull<UserProfile>()
                    withContext(Dispatchers.Main) {
                        userProfile = profile
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }
            try {
                val result = SupabaseProvider.client.from("items")
                    .select(Columns.list("*"))
                    .decodeList<ItemListing>()
                withContext(Dispatchers.Main) {
                    listings = result
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    errorMessage = t.localizedMessage ?: "Failed to load items from database."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
}
