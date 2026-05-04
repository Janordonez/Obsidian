package com.example.obsidian.ui.components

import androidx.compose.foundation.Canvas // <-- Corregido
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset // <-- Import necesario añadido
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.contracts.CallsInPlace

@Composable
fun Grid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // En DrawScope, toPx() es una función y lleva paréntesis
        val gridSize = 50.dp.toPx()
        val color = Color(0xFF3F3F3F)   // Muy sutil

        // Usamos un bucle while con Float para evitar errores de redondeo de píxeles
        // Líneas verticales
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            x += gridSize
        }

        // Líneas horizontales
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += gridSize
        }
    }
}