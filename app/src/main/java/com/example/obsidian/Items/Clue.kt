package com.example.obsidian.Items

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Clue(
    val nombre: String,
    val descripcion: String,
    val imagen: Int,
    var fueRevelada: Boolean = false,
    val objetivoX: Float, // Coordenada donde debe encajar
    val objetivoY: Float
){
    var actualX by mutableStateOf(0f)
    var actualY by mutableStateOf(0f)
    var estaEncajada by mutableStateOf(false)
}
