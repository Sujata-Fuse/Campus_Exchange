package com.example.campus_exchange_final.feature.activity

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

class ActivityViewModel : ViewModel() {
    var activities by mutableStateOf<List<ActivityItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchActivities()
    }

    fun fetchActivities() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = SupabaseProvider.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val result = SupabaseProvider.client.from("borrow_requests")
                        .select(Columns.list("*"))
                        .decodeList<com.example.campus_exchange_final.feature.borrow.BorrowRequestPayload>()

                    activities = result.mapIndexed { index, req ->
                        ActivityItem(
                            id = index.toString(),
                            title = "Request Status: ${req.status}",
                            subtitle = req.message.ifBlank { "Borrow Request" },
                            timeAgo = req.duration
                        )
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load activity."
            } finally {
                isLoading = false
            }
        }
    }
}
