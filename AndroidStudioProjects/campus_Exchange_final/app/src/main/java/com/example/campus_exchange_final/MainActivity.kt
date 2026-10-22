package com.example.campus_exchange_final

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.campus_exchange_final.navigation.CampusExchangeApp
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.Campus_Exchange_finalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force software rendering layer type to bypass emulator GPU hardware overlay black screen issues
        window.decorView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        enableEdgeToEdge()
        setContent {
            Campus_Exchange_finalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppBackground
                ) {
                    CampusExchangeApp(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
