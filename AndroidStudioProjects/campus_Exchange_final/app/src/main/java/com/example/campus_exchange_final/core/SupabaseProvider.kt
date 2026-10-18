package com.example.campus_exchange_final.core

import com.example.campus_exchange_final.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    val client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth)
                install(Postgrest)
            }
        } catch (_: Throwable) {
            createSupabaseClient(
                supabaseUrl = "https://your-project.supabase.co",
                supabaseKey = "your-publishable-key"
            ) {
                install(Auth)
                install(Postgrest)
            }
        }
    }
}
