package com.example.obsidian.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameCase(
    val title: String,
    val description: String,
    val suspects: List<AISuspect>,
    val clues: List<AIClue>,
    val guiltyId: String = "" // ID of the guilty suspect
)

@Serializable
data class AISuspect(
    val id: String,
    val name: String,
    val gender: String,
    val personality: String,
    val background: String,
    val relation: String,
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

data class GameResult(
    val isCorrect: Boolean,
    val accusedSuspectName: String,
    val actualGuiltyName: String,
    val epilogue: String,
    val correctClueAssignments: Int,
    val totalClues: Int
)
