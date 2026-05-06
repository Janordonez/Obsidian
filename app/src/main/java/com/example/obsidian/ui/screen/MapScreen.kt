package com.example.obsidian.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.obsidian.ui.theme.CyanNeon
import com.example.obsidian.ui.viewmodel.GameViewModel

@Composable
fun MapScreen(
    navController: NavController, 
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameCase by viewModel.currentCase.collectAsStateWithLifecycle()
    val isGenerating = viewModel.isGenerating
    val clues = gameCase?.clues ?: emptyList()

    if (isGenerating) {
        Box(modifier = modifier.fillMaxSize().background(Color(0xFF09090B)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CyanNeon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("MAPEANDO SECTOR...", color = CyanNeon, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // zinc-950
    ) {
        // Digital Grid Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 30.dp.toPx()
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CyanNeon.copy(alpha = 0.05f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CyanNeon.copy(alpha = 0.05f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        // Scanlines effect
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            for (y in 0..size.height.toInt() step 4) {
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 2f
                )
            }
        }

        // Points of Interest (Clues)
        clues.forEachIndexed { index, clue ->
            // Simulating coordinates by mapping index to screen position
            val xOffset = when(index) {
                0 -> 0.2f
                1 -> 0.7f
                else -> 0.4f
            }
            val yOffset = when(index) {
                0 -> 0.3f
                1 -> 0.6f
                else -> 0.8f
            }
            
            MapMarker(
                title = clue.locationName,
                subtitle = "PUNTO DE INTERÉS",
                modifier = Modifier.align { _, containerSize, _ ->
                    IntOffset(
                        (containerSize.width * xOffset).toInt(),
                        (containerSize.height * yOffset).toInt()
                    )
                }
            )
        }

        // Current Location Marker
        CurrentLocationMarker(
            modifier = Modifier.align(Alignment.Center)
        )

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "OBSIDIAN CASE FILE",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = CyanNeon
                    )
                )
            }
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }

        // Mission Status Card (Floating)
        Column(
            modifier = Modifier
                .padding(top = 80.dp, start = 24.dp)
                .width(280.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTADO DE MISIÓN",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(modifier = Modifier.size(8.dp).background(CyanNeon, CircleShape))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = gameCase?.description ?: "Localiza pistas en el sector.",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFF1F1F1F))) {
                Box(modifier = Modifier.fillMaxWidth(0.65f).fillMaxHeight().background(CyanNeon))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SEGUIMIENTO: OBSIDIAN", color = Color.Gray, fontSize = 8.sp)
                Text("65%", color = Color.Gray, fontSize = 8.sp)
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .padding(top = 280.dp, start = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ESCANEAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("FILTROS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Coordinates display
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .border(width = 2.dp, color = CyanNeon.copy(alpha = 0.3f), shape = RoundedCornerShape(0.dp))
                    .padding(end = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(4.dp)) {
                    Text("LAT: 35.6895° N", color = CyanNeon.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("LNG: 139.6917° E", color = CyanNeon.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("ALT: 24M", color = CyanNeon.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun MapMarker(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            color = Color.Black.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(2.dp)
        ) {
            Icon(Icons.Default.Radar, contentDescription = null, tint = CyanNeon, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.8f))
                .border(width = 2.dp, color = CyanNeon, shape = RoundedCornerShape(0.dp)) // Left border simulated by being part of a custom draw or just using a box
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = subtitle, color = CyanNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CurrentLocationMarker(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Pulse effect
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = pulseAlpha))
                    .blur(16.dp)
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(CyanNeon, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.9f))
                .border(1.dp, CyanNeon.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("UBICACIÓN ACTUAL", color = CyanNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("SECTOR DE INVESTIGACIÓN", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
