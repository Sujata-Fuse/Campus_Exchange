package com.example.campus_exchange_final.feature.lostfound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_exchange_final.core.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class LostFoundViewModel : ViewModel() {
    var reports by mutableStateOf<List<LostFoundItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchReports()
    }

    fun fetchReports() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = SupabaseProvider.client.from("lost_found_reports")
                    .select(Columns.list("*"))
                    .decodeList<LostFoundItem>()
                reports = result
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load lost & found reports."
            } finally {
                isLoading = false
            }
        }
    }

    fun submitReport(
        title: String,
        isLost: Boolean,
        category: String,
        location: String,
        description: String,
        onsuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val report = LostFoundItem(
                        userId = userId,
                        title = title,
                        isLost = isLost,
                        category = category,
                        location = location,
                        description = description
                    )
                    SupabaseProvider.client.from("lost_found_reports").insert(report)
                    onsuccess()
                } else {
                    errorMessage = "Must be logged in to report an item."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to submit report."
            } finally {
                isLoading = false
            }
        }
    }
}
