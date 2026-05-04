package com.example.obsidian.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.obsidian.Items.Clue
import com.example.obsidian.Items.CluesRepository
import com.example.obsidian.ui.theme.SpaceGrotesk
import com.example.obsidian.ui.theme.primary
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun ClueItem() {
    val clues = remember {CluesRepository().getClues()}

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start, // Alinea al inicio
        verticalAlignment = Alignment.CenterVertically
    ) {
        clues.forEach { clue ->
            // Contenedor individual para cada pista
            if (clue.fueRevelada && !clue.estaEncajada) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(clue.actualX.roundToInt(), clue.actualY.roundToInt()) }
                        .pointerInput(clue.estaEncajada) {

                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume() // CRUCIAL: Detiene el scroll del padre
                                    clue.actualX += dragAmount.x
                                    clue.actualY += dragAmount.y
                                },
                                onDragEnd = {
                                    // Lógica para encajar (puedes usar un umbral de distancia)
                                    val dist = hypot(
                                        (clue.actualX - clue.objetivoX).toDouble(),
                                        (clue.actualY - clue.objetivoY).toDouble()
                                    )
                                    if (dist < 150f) {
                                        clue.actualX = clue.objetivoX
                                        clue.actualY = clue.objetivoY
                                    }
                                }
                            )
                        }) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp, 4.dp, 8.dp, 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Box(
                            modifier = Modifier
                                .size(64.dp) // Es importante dar un tamaño si la imagen es pequeña
                                .border(1.dp, Color(0xFF4D4632)),


                            ) {

                            Image(
                                painter = painterResource(id = clue.imagen),
                                contentDescription = clue.nombre,
                                modifier = Modifier.fillMaxSize()
                            )


                        }

                        Text(
                            text = clue.nombre,
                            modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                            fontFamily = SpaceGrotesk,
                            fontSize = 12.sp,
                            color = Color(0xFFD1C6AB)
                        )
                    }
                }
            }
        }
    }
}