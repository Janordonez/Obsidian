package com.example.obsidian.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.obsidian.Items.Clue
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun PiezaTablon(
    clue: Clue,
    zonasValidas: List<Offset> // <-- 1. Recibimos los lugares donde se puede encajar
) {
    Column(
        modifier = Modifier
            .offset { IntOffset(clue.actualX.roundToInt(), clue.actualY.roundToInt()) }
            .shadow(4.dp)
            .background(Color.White)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    // 2. ¿Qué pasa cuando el usuario arrastra el dedo? (Igual que antes)
                    onDrag = { change, dragAmount ->
                        change.consume()
                        clue.actualX += dragAmount.x
                        clue.actualY += dragAmount.y
                    },
                    // 3. ¿Qué pasa cuando el usuario SUELTA la pieza?
                    onDragEnd = {
                        val radioAtraccion =
                            150f // Distancia en píxeles para que el "imán" haga efecto
                        var encajado = false

                        for (zona in zonasValidas) {
                            // Calculamos la distancia entre la pieza y la zona válida (Teorema de Pitágoras)
                            val distanciaX = clue.actualX - zona.x
                            val distanciaY = clue.actualY - zona.y
                            val distanciaTotal =
                                sqrt((distanciaX * distanciaX + distanciaY * distanciaY).toDouble()).toFloat()

                            if (distanciaTotal < radioAtraccion) {
                                // ¡SNAP! La pieza está lo bastante cerca, la encajamos al centro del hueco
                                clue.actualX = zona.x
                                clue.actualY = zona.y
                                encajado = true
                                break // Ya encajó, no revisamos más zonas
                            }
                        }


                        if (!encajado) {
                            clue.actualX = 0f // O la posición inicial original de la pieza
                            clue.actualY = 0f
                        }
                        clue.estaEncajada = encajado
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(100.dp)) {
            Image(
                painter = painterResource(clue.imagen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = clue.nombre,
            modifier = Modifier.padding(vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}