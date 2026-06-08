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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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

    var isDescriptionCollapsed by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    offset += pan
                }
            }
    ) {
        // Digital Grid Overlay (Pannable)
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            val gridSize = 30.dp.toPx()
            val width = size.width * 5
            val height = size.height * 5
            
            for (x in -width.toInt()..width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CyanNeon.copy(alpha = 0.05f),
                    start = Offset(x.toFloat(), -height),
                    end = Offset(x.toFloat(), height),
                    strokeWidth = 1f
                )
            }
            for (y in -height.toInt()..height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = CyanNeon.copy(alpha = 0.05f),
                    start = Offset(-width, y.toFloat()),
                    end = Offset(width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }

        // Scanlines effect (Static)
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

        // Points of Interest (Clues) - Pannable
        Box(modifier = Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            clues.forEachIndexed { index, clue ->
                // Mapping index to screen position
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
        }

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
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

        // Mission Status Card (Floating & Collapsible)
        Column(
            modifier = Modifier
                .padding(top = 80.dp, start = 24.dp)
                .width(280.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.1f))
                .clickable { isDescriptionCollapsed = !isDescriptionCollapsed }
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
                Box(modifier = Modifier.size(8.dp).background(if (isDescriptionCollapsed) Color.Gray else CyanNeon, CircleShape))
            }
            if (!isDescriptionCollapsed) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = gameCase?.description ?: "Localiza pistas en el sector.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "(Toca para contraer)",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "(Toca para expandir)",
                    color = CyanNeon.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
                .border(width = 2.dp, color = CyanNeon, shape = RoundedCornerShape(0.dp))
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
