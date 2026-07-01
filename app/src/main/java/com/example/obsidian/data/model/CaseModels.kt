package com.example.obsidian.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    INTRO, EXPLORE_SCENE, INTERROGATION, ANALYZE_EVIDENCE, ACCUSATION, VERDICT
}

@Serializable
data class InvestigationProgress(
    val discoveredClues: Int = 0,
    val solvedMinigames: Int = 0,
    val interrogationsCompleted: Int = 0,
    val contradictionsFound: Int = 0,
    val deductionPoints: Int = 0,
    val explorationCount: Int = 0
)

@Serializable
data class Solution(
    val guiltySuspectId: String,
    val motive: String,
    val keyEvidenceId: String
)

@Serializable
data class Case(
    val id: String,
    val title: String,
    val description: String,
    val suspects: List<Suspect>,
    val clues: List<Clue>,
    val solution: Solution
)

@Serializable
data class Suspect(
    val id: String,
    val name: String,
    val gender: String = "MALE",
    val personality: String,
    val alibi: String,
    val contradictions: List<String>,
    val trustLevel: Int = 50,
    val relation: String = "",
    val room: String = "",
    val imageId: Int = 0,
    val availableQuestions: List<Question> = emptyList()
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val effectType: String = "NONE", // "TRUST", "UNLOCK_CLUE", "CONTRADICTION"
    val effectValue: String = "",
    val coherence: Int = 50, // 0-100: mayor coherencia = más probabilidad de revelar información
    val topic: String = "general",
    val approach: String = "NEUTRAL" // EMPÁTICO, DIRECTO, PRESIÓN, TÉCNICO — visible al jugador, no probabilidad
)

@Serializable
data class InterrogationSession(
    val suspectId: String,
    val timeRemainingSeconds: Int = 90,
    val questionsThisRound: Int = 0,
    val maxQuestionsPerRound: Int = 3,
    val askedQuestionIds: Set<String> = emptySet(),
    val isActive: Boolean = true,
    val currentTopicIndex: Int = 0,
    val sessionKeyPoints: List<String> = emptyList()
)

@Serializable
data class InterrogationSummary(
    val suspectId: String,
    val suspectName: String,
    val keyPoints: List<String>,
    val contradictionsFound: List<String>,
    val cluesUnlocked: List<String>,
    val questionsAsked: Int,
    val completedAt: String
)

@Serializable
data class Clue(
    val id: String,
    val title: String,
    val description: String,
    val linkedSuspects: List<String>, 
    val importance: Int,
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = false,
    val isFound: Boolean = false,
    val unlockConditionType: String = "START", 
    val unlockConditionValue: String = ""
)

@Serializable
data class Statement(
    val id: String,
    val suspectId: String,
    val text: String,
    val questionId: String
)

@Serializable
data class GameState(
    val currentCase: Case? = null,
    val discoveredClues: List<Clue> = emptyList(),
    val interrogatedSuspects: List<String> = emptyList(),
    val explorationCount: Int = 0,
    val phase: GamePhase = GamePhase.INTRO,
    val score: Int = 0,
    val progress: InvestigationProgress = InvestigationProgress(),
    val clueAssignments: Map<String, String> = emptyMap(), // clueId -> suspectId
    val messages: Map<String, List<InterrogationMessage>> = emptyMap(),
    val gameResult: GameResult? = null,
    val minigameResults: Map<String, String> = emptyMap(), // locationName -> "WON" / "LOST" / "PARTIAL"
    val blockedLocations: Map<String, Int> = emptyMap(), // locationName -> unlockAtExplorationCount
    val hasSafeSecretLetter: Boolean = false,
    val extraInfoFound: List<String> = emptyList(),
    val statements: List<Statement> = emptyList(),
    val discoveredContradictions: Set<String> = emptySet(),
    val suspicionLevels: Map<String, Int> = emptyMap(),
    val hasPressedCarlos: Boolean = false,
    val interrogationSessions: Map<String, InterrogationSession> = emptyMap(),
    val interrogationSummaries: Map<String, InterrogationSummary> = emptyMap()
)

@Serializable
data class DeductionResult(
    val isValid: Boolean,
    val message: String,
    val suspectId: String? = null
)

@Serializable
data class InterrogationMessage(
    val sender: String,
    val text: String,
    val isDetective: Boolean,
    val time: String,
    val isContradiction: Boolean = false
)

@Serializable
data class GameResult(
    val isCorrect: Boolean,
    val accusedSuspectName: String,
    val actualGuiltyName: String,
    val epilogue: String,
    val correctClueAssignments: Int,
    val totalClues: Int,
    val rank: String = "",
    val totalContradictions: Int = 0,
    val foundContradictions: Int = 0,
    val totalSuspects: Int = 0,
    val interrogatedCount: Int = 0,
    val explorationCount: Int = 0,
    val missingHints: List<String> = emptyList(),
    val score: Int = 0
)
