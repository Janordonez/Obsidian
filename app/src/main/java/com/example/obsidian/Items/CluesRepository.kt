package com.example.obsidian.Items

import androidx.compose.runtime.mutableStateOf
import com.example.obsidian.R

class CluesRepository {

    private val inventarioPistas = mutableListOf<Clue>(
        Clue("Pase de Abordar", "Pase de abordar al barco a las 11:00 AM", R.drawable.clue1, true,0f, 0f),
        Clue("Pase de Abordar", "Pase de abordar al barco a las 11:00 AM", R.drawable.clue1, true, 0f, 0f)
    )

    fun getClues(): List<Clue> = inventarioPistas


}