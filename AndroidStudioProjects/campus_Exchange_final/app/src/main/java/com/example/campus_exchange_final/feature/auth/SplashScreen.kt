package com.example.campus_exchange_final.feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campus_exchange_final.ui.theme.AppBackground
import com.example.campus_exchange_final.ui.theme.BrandPurple
import com.example.campus_exchange_final.ui.theme.Campus_Exchange_finalTheme
import com.example.campus_exchange_final.ui.theme.TextPrimary
import com.example.campus_exchange_final.ui.theme.TextSecondary

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Subtle Background Logo Watermark (Large, aesthetically positioned behind content)
        BackgroundLogoWatermark(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
        )

        // Main Content (Centered)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Icon Container - Made slightly larger and more prominent
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(BrandPurple),
                contentAlignment = Alignment.Center
            ) {
                // Mortarboard Cap Icon
                MortarboardCapIcon(
                    modifier = Modifier.size(54.dp),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "Campus",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Exchange",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPurple
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle Tagline
            Text(
                text = "Borrow  •  Lend  •  Reconnect",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Campus Illustration
        CampusIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BackgroundLogoWatermark(
    modifier: Modifier = Modifier,
    color: Color = BrandPurple.copy(alpha = 0.06f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Cap Diamond (Top)
        val diamondPath = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.95f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.61f)
            lineTo(w * 0.05f, h * 0.38f)
            close()
        }
        drawPath(diamondPath, color = color)

        // Cap Base / Band
        val bandPath = Path().apply {
            moveTo(w * 0.25f, h * 0.52f)
            lineTo(w * 0.25f, h * 0.74f)
            cubicTo(
                w * 0.35f, h * 0.86f,
                w * 0.65f, h * 0.86f,
                w * 0.75f, h * 0.74f
            )
            lineTo(w * 0.75f, h * 0.52f)
            close()
        }
        drawPath(bandPath, color = color)
    }
}

@Composable
fun MortarboardCapIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Cap Diamond (Top)
        val diamondPath = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.9f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.56f)
            lineTo(w * 0.1f, h * 0.38f)
            close()
        }
        drawPath(diamondPath, color = color)

        // Cap Base / Band (Underneath)
        val bandPath = Path().apply {
            moveTo(w * 0.28f, h * 0.49f)
            lineTo(w * 0.28f, h * 0.68f)
            cubicTo(
                w * 0.38f, h * 0.78f,
                w * 0.62f, h * 0.78f,
                w * 0.72f, h * 0.68f
            )
            lineTo(w * 0.72f, h * 0.49f)
            close()
        }
        drawPath(bandPath, color = color.copy(alpha = 0.9f))

        // Tassel
        val tasselPath = Path().apply {
            moveTo(w * 0.82f, h * 0.42f)
            lineTo(w * 0.82f, h * 0.72f)
        }
        drawPath(
            tasselPath,
            color = color,
            style = Stroke(width = w * 0.06f)
        )
        // Tassel Endpoint
        drawCircle(
            color = color,
            radius = w * 0.05f,
            center = Offset(w * 0.82f, h * 0.74f)
        )
    }
}

@Composable
private fun CampusIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background Hill/Ground Layer 1
        val groundPath1 = Path().apply {
            moveTo(0f, h * 0.7f)
            quadraticTo(w * 0.3f, h * 0.55f, w * 0.65f, h * 0.72f)
            quadraticTo(w * 0.85f, h * 0.82f, w, h * 0.68f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(groundPath1, color = Color(0xFFEBF2FF))

        // Background Hill/Ground Layer 2
        val groundPath2 = Path().apply {
            moveTo(0f, h * 0.82f)
            quadraticTo(w * 0.4f, h * 0.72f, w * 0.75f, h * 0.85f)
            quadraticTo(w * 0.9f, h * 0.9f, w, h * 0.82f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(groundPath2, color = Color(0xFFDFE9FF))

        // Trees Left
        val treeColor1 = Color(0xFF00B894).copy(alpha = 0.8f)
        drawCircle(color = treeColor1, radius = w * 0.08f, center = Offset(w * 0.12f, h * 0.68f))
        drawCircle(color = Color(0xFF74B9FF).copy(alpha = 0.8f), radius = w * 0.06f, center = Offset(w * 0.20f, h * 0.72f))

        // Trees Right
        drawCircle(color = treeColor1, radius = w * 0.07f, center = Offset(w * 0.88f, h * 0.70f))
        drawCircle(color = Color(0xFF8075FF).copy(alpha = 0.7f), radius = w * 0.06f, center = Offset(w * 0.80f, h * 0.74f))

        // Main Campus Building Center Spire & Towers
        val buildingPurple = Color(0xFF6C5CE7).copy(alpha = 0.85f)
        val buildingLightPurple = Color(0xFF8075FF).copy(alpha = 0.9f)
        val buildingRoofBlue = Color(0xFF74B9FF)

        // Center Main Spire Tower Body
        val towerWidth = w * 0.16f
        val towerLeft = (w - towerWidth) / 2
        drawRect(
            color = buildingPurple,
            topLeft = Offset(towerLeft, h * 0.38f),
            size = Size(towerWidth, h * 0.45f)
        )

        // Center Clock/Window Circle
        drawCircle(
            color = Color.White,
            radius = towerWidth * 0.22f,
            center = Offset(w / 2, h * 0.46f)
        )

        // Center Spire Roof (Triangle)
        val spirePath = Path().apply {
            moveTo(w / 2, h * 0.18f)
            lineTo(towerLeft - w * 0.02f, h * 0.38f)
            lineTo(towerLeft + towerWidth + w * 0.02f, h * 0.38f)
            close()
        }
        drawPath(spirePath, color = buildingRoofBlue)

        // Left Building Wing
        drawRect(
            color = buildingLightPurple,
            topLeft = Offset(w * 0.28f, h * 0.50f),
            size = Size(w * 0.18f, h * 0.33f)
        )
        val leftRoofPath = Path().apply {
            moveTo(w * 0.37f, h * 0.38f)
            lineTo(w * 0.26f, h * 0.50f)
            lineTo(w * 0.48f, h * 0.50f)
            close()
        }
        drawPath(leftRoofPath, color = buildingRoofBlue)

        // Right Building Wing
        drawRect(
            color = buildingLightPurple,
            topLeft = Offset(w * 0.54f, h * 0.50f),
            size = Size(w * 0.18f, h * 0.33f)
        )
        val rightRoofPath = Path().apply {
            moveTo(w * 0.63f, h * 0.38f)
            lineTo(w * 0.52f, h * 0.50f)
            lineTo(w * 0.74f, h * 0.50f)
            close()
        }
        drawPath(rightRoofPath, color = buildingRoofBlue)

        // Windows on Left/Right Wings
        val windowColor = Color.White.copy(alpha = 0.9f)
        drawRect(color = windowColor, topLeft = Offset(w * 0.32f, h * 0.55f), size = Size(w * 0.035f, h * 0.06f))
        drawRect(color = windowColor, topLeft = Offset(w * 0.39f, h * 0.55f), size = Size(w * 0.035f, h * 0.06f))
        drawRect(color = windowColor, topLeft = Offset(w * 0.58f, h * 0.55f), size = Size(w * 0.035f, h * 0.06f))
        drawRect(color = windowColor, topLeft = Offset(w * 0.65f, h * 0.55f), size = Size(w * 0.035f, h * 0.06f))

        // Clouds & Sky Sparkles
        val cloudColor = Color.White.copy(alpha = 0.85f)
        drawCircle(color = cloudColor, radius = w * 0.05f, center = Offset(w * 0.18f, h * 0.28f))
        drawCircle(color = cloudColor, radius = w * 0.07f, center = Offset(w * 0.23f, h * 0.26f))
        drawCircle(color = cloudColor, radius = w * 0.05f, center = Offset(w * 0.28f, h * 0.29f))

        drawCircle(color = cloudColor, radius = w * 0.05f, center = Offset(w * 0.72f, h * 0.24f))
        drawCircle(color = cloudColor, radius = w * 0.07f, center = Offset(w * 0.77f, h * 0.22f))
        drawCircle(color = cloudColor, radius = w * 0.05f, center = Offset(w * 0.82f, h * 0.25f))

        // Sparkles/Stars
        drawCircle(color = Color(0xFFFDCB6E), radius = w * 0.012f, center = Offset(w * 0.12f, h * 0.18f))
        drawCircle(color = Color(0xFF8075FF), radius = w * 0.015f, center = Offset(w * 0.88f, h * 0.15f))
        drawCircle(color = Color(0xFF00B894), radius = w * 0.01f, center = Offset(w * 0.42f, h * 0.12f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    Campus_Exchange_finalTheme {
        SplashScreen()
    }
}
