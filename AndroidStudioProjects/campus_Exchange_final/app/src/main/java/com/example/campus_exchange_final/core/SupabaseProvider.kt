package com.example.campus_exchange_final.core

import com.example.campus_exchange_final.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

object SupabaseProvider {
    val client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth)
                install(Postgrest)
                httpEngine = Android.create {
                    connectTimeout = 60_000
                    socketTimeout = 60_000
                }
            }
        } catch (_: Throwable) {
            createSupabaseClient(
                supabaseUrl = "https://slqurofkjyihkommvxnu.supabase.co",
                supabaseKey = "sb_publishable_Ba963hMRanb6KqZuifOjMQ_MLiD3X-K"
            ) {
                install(Auth)
                install(Postgrest)
                httpEngine = Android.create {
                    connectTimeout = 60_000
                    socketTimeout = 60_000
                }
            }
        }
    }
}
