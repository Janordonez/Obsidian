package com.example.obsidian.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.obsidian.Items.CluesRepository
import com.example.obsidian.ui.components.ClueItem
import com.example.obsidian.ui.components.CyberBottomBar
import com.example.obsidian.ui.components.Grid
import com.example.obsidian.ui.components.PiezaTablon
import com.example.obsidian.ui.theme.SpaceGrotesk
import com.example.obsidian.ui.theme.blanco
import kotlin.math.roundToInt

@Composable
fun CaseScreen(navController: NavController, modifier: Modifier = Modifier) {
    // Dentro de tu CaseScreen
    val scale = remember { mutableStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    val espaciosDelTablon = listOf(
        Offset(2000f, 2000f),
        Offset(2400f, 2400f),
        Offset(2800f, 2800f)
    )

    val pistas = remember { CluesRepository().getClues() }

    Scaffold(
        modifier = Modifier.fillMaxSize(), containerColor = Color(0xFF09090B), // zinc-950
        bottomBar = {
            CyberBottomBar(navController)
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Detectar zoom y paneo del tablón completo
                            detectTransformGestures { _, pan, zoom, _ ->

                                offset.value += pan
                            }
                        }

                        .graphicsLayer(
                            scaleX = scale.value,
                            scaleY = scale.value,
                            translationX = offset.value.x,
                            translationY = offset.value.y
                        ) // Color del fondo del tablón
                ) {
                    Box(
                        modifier = Modifier
                            .requiredSize(2000.dp, 2000.dp)
                            .align(Alignment.Center) // Centralo para que el zoom out se vea bien
                            .background(Color(0xFF0B1326)) // El fondo Zinc-950
                    ) {
                        // Dibujamos una cuadrícula para que el usuario sienta el movimiento

                        Grid()

                        espaciosDelTablon.forEach { zona ->
                            // Comprobamos si hay alguna pista ocupando exactamente esta zona
                            val zonaOcupada = pistas.any { pista ->
                                pista.estaEncajada && pista.actualX == zona.x && pista.actualY == zona.y
                            }

                            // Si NO está ocupada, dibujamos el cuadrado oscuro
                            if (!zonaOcupada) {
                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(zona.x.roundToInt(), zona.y.roundToInt()) }
                                        // 108.dp para que coincida con tu foto (100dp) + el padding (4dp por lado)
                                        .size(108.dp)
                                        // Un color más oscuro que el fondo para que parezca una ranura
                                        .background(Color(0xFF04060A))
                                        // Opcional: un borde sutil para que se note más
                                        .border(1.dp, Color.White.copy(alpha = 0.1f))
                                )
                            }
                        }


                        pistas.forEach { pista ->
                            if(pista.estaEncajada)
                                PiezaTablon(
                                    clue = pista,
                                    zonasValidas = espaciosDelTablon
                                )
                        }

                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0XFF060E20))
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0XFF131B2E))
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2

                        // Border TOP
                        drawLine(
                            color = Color(0XFF4D4632),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )

                        // Border BOTTOM
                        drawLine(
                            color = Color(0XFF4D4632),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }){
                    Text(
                        text = "EVIDENCIAS SIN ASIGNAR",
                        fontFamily = SpaceGrotesk,
                        fontSize = 12.sp,
                        color = Color(0xFFD1C6AB),
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(10.dp, 0.dp, 10.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start){

                    ClueItem()
                }
            }
        }
    }
}
