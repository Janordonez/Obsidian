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
    val gender: String, // "MALE" or "FEMALE"
    val personality: String,
    val background: String,
    val relation: String, // E.g., "Esposa del asesinado"
    val tension: Float,
    val status: String,
    val bpm: Int,
    val caseNumber: String,
    val room: String,
    val alibi: String = "",
    val imageId: Int = 0 
)

@Serializable
data class AIClue(
    val id: Int,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val ownerSuspectId: String, // The ID of the suspect this clue incriminates
    val isFound: Boolean = false
)

data class InterrogationMessage(
    val sender: String,
    val text: String,
    val isDetective: Boolean,
    val time: String
)
