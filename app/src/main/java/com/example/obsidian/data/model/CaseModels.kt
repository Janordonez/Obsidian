package com.example.obsidian.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCase(
    val title: String,
    val description: String,
    val suspects: List<AISuspect>,
    val clues: List<AIClue>
)

@Serializable
data class AISuspect(
    val id: String,
    val name: String,
    val alias: String,
    val personality: String,
    val background: String,
    val tension: Float,
    val status: String,
    val bpm: Int,
    val caseNumber: String,
    val room: String,
    val alibi: String = "",
    val imageId: Int = 0 // Will map to a real R.drawable ID in the ViewModel
)

@Serializable
data class AIClue(
    val id: Int,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val isFound: Boolean = false
)

data class InterrogationMessage(
    val sender: String,
    val text: String,
    val isDetective: Boolean,
    val time: String
)
