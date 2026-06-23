package com.example.obsidian.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.obsidian.BuildConfig
import com.example.obsidian.data.model.*
import com.example.obsidian.data.remote.GeminiService
import com.example.obsidian.data.remote.GroqService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)
    private val groqService = GroqService(BuildConfig.GROQ_API_KEY)
    private val sharedPrefs = application.getSharedPreferences("obsidian_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<InterrogationMessage>>>(emptyMap())
    val messages = _messages.asStateFlow()

    private val _selectedClues = MutableStateFlow<Set<String>>(emptySet())
    val selectedClues = _selectedClues.asStateFlow()

    private val _deductionResult = MutableStateFlow<DeductionResult?>(null)
    val deductionResult = _deductionResult.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult = _gameResult.asStateFlow()

    // Configuración de Audio
    var isEffectsEnabled by mutableStateOf(sharedPrefs.getBoolean("effects_enabled", true))
    var effectsVolume by mutableFloatStateOf(sharedPrefs.getFloat("effects_volume", 0.7f))
    var isMusicEnabled by mutableStateOf(sharedPrefs.getBoolean("music_enabled", true))
    var musicVolume by mutableFloatStateOf(sharedPrefs.getFloat("music_volume", 0.5f))

    var isGenerating by mutableStateOf(false)
        private set

    var isSubmittingAccusation by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init { loadSavedCase() }

    private fun loadSavedCase() {
        val savedCaseJson = sharedPrefs.getString("current_case", null)
        if (savedCaseJson != null) {
            try {
                val savedCase = json.decodeFromString<Case>(savedCaseJson)
                _gameState.value = _gameState.value.copy(
                    currentCase = savedCase, 
                    phase = GamePhase.EXPLORE_SCENE
                )
            } catch (e: Exception) { Log.e("GameViewModel", "Error loading case", e) }
        }
    }

    fun saveSettings() {
        sharedPrefs.edit().apply {
            putBoolean("effects_enabled", isEffectsEnabled)
            putFloat("effects_volume", effectsVolume)
            putBoolean("music_enabled", isMusicEnabled)
            putFloat("music_volume", musicVolume)
            apply()
        }
    }

    fun startNewInvestigation() {
        viewModelScope.launch {
            isGenerating = true
            errorMessage = null
            _selectedClues.value = emptySet()
            _deductionResult.value = null
            _gameResult.value = null
            try {
                val newCase = geminiService.generateCase(3) ?: groqService.generateCase(3)
                if (newCase != null) {
                    _gameState.value = GameState(currentCase = newCase, phase = GamePhase.EXPLORE_SCENE)
                    saveCase(newCase)
                    _messages.value = newCase.suspects.associate { it.id to listOf(
                        InterrogationMessage("SISTEMA", "EXPEDIENTE INICIADO", false, getCurrentTime())
                    ) }
                } else { errorMessage = "Error de conexión con IA." }
            } catch (e: Exception) { errorMessage = e.message }
            isGenerating = false
        }
    }

    private fun saveCase(case: Case) {
        sharedPrefs.edit().putString("current_case", json.encodeToString(case)).apply()
    }

    fun exploreScene() {
        _gameState.update { it.copy(explorationCount = it.explorationCount + 1) }
        checkAndUnlockClues()
    }

    fun collectClue(clueId: String) {
        _gameState.update { state ->
            val case = state.currentCase ?: return@update state
            val updatedClues = case.clues.map { 
                if (it.id == clueId) it.copy(isFound = true) else it 
            }
            state.copy(currentCase = case.copy(clues = updatedClues))
        }
        checkAndUnlockClues()
    }

    private fun checkAndUnlockClues() {
        _gameState.update { state ->
            val case = state.currentCase ?: return@update state
            val updatedClues = case.clues.map { clue ->
                if (clue.isAvailable) return@map clue
                
                val isUnlocked = when (clue.unlockConditionType) {
                    "INTERROGATION" -> state.interrogatedSuspects.contains(clue.unlockConditionValue)
                    "EXPLORATION" -> state.explorationCount >= (clue.unlockConditionValue.toIntOrNull() ?: 2)
                    "CLUE" -> case.clues.any { it.id == clue.unlockConditionValue && it.isFound }
                    "START" -> true
                    else -> false
                }
                
                if (isUnlocked) clue.copy(isAvailable = true) else clue
            }
            state.copy(currentCase = case.copy(clues = updatedClues))
        }
    }

    fun onQuestionSelected(suspect: Suspect, question: Question) {
        updateMessages(suspect.id, InterrogationMessage("INVESTIGADOR", question.text, true, getCurrentTime()))
        
        _gameState.update { state ->
            val case = state.currentCase ?: return@update state
            var updatedSuspect = suspect
            when (question.effectType) {
                "TRUST" -> {
                    val change = question.effectValue.toIntOrNull() ?: 0
                    updatedSuspect = suspect.copy(trustLevel = (suspect.trustLevel + change).coerceIn(0, 100))
                }
                "CONTRADICTION" -> {
                    updatedSuspect = suspect.copy(contradictions = suspect.contradictions + question.effectValue)
                }
            }
            val updatedSuspects = case.suspects.map { if (it.id == suspect.id) updatedSuspect else it }
            state.copy(
                currentCase = case.copy(suspects = updatedSuspects), 
                interrogatedSuspects = (state.interrogatedSuspects + suspect.id).distinct()
            )
        }
        checkAndUnlockClues()
        
        viewModelScope.launch {
            val history = _messages.value[suspect.id]?.takeLast(5)?.map { (if (it.isDetective) "Detective" else suspect.name) to it.text } ?: emptyList()
            val response = geminiService.getSuspectResponse(suspect, history, question.text)
            updateMessages(suspect.id, InterrogationMessage(suspect.name, response, false, getCurrentTime()))
        }
    }

    fun sendMessage(suspect: Suspect, text: String) {
        updateMessages(suspect.id, InterrogationMessage("INVESTIGADOR", text, true, getCurrentTime()))
        viewModelScope.launch {
            val history = _messages.value[suspect.id]?.takeLast(5)?.map { (if (it.isDetective) "Detective" else suspect.name) to it.text } ?: emptyList()
            val response = geminiService.getSuspectResponse(suspect, history, text)
            updateMessages(suspect.id, InterrogationMessage(suspect.name, response, false, getCurrentTime()))
        }
    }

    fun toggleClueSelection(clueId: String) {
        _selectedClues.update { current ->
            if (current.contains(clueId)) current - clueId else current + clueId
        }
    }

    fun evaluateDeduction() {
        val case = gameState.value.currentCase ?: return
        val selectedIds = _selectedClues.value
        
        if (selectedIds.size < 2) {
            _deductionResult.value = DeductionResult(false, "Conecta al menos 2 pistas.")
            return
        }

        val selectedClueObjects = case.clues.filter { it.id in selectedIds }
        val commonSuspects = selectedClueObjects
            .map { it.linkedSuspects.toSet() }
            .reduce { acc, suspects -> acc intersect suspects }

        viewModelScope.launch {
            if (commonSuspects.isNotEmpty()) {
                val suspectId = commonSuspects.first()
                val suspect = case.suspects.find { it.id == suspectId }
                
                val analysis = groqService.analyzeClueConnection(case.title, suspect?.name ?: "Sospechoso", selectedClueObjects.map { it.title })
                
                _deductionResult.value = DeductionResult(
                    isValid = true,
                    message = analysis,
                    suspectId = suspectId
                )
                updateSuspectTrust(suspectId, (suspect?.trustLevel ?: 50) - 15)
            } else {
                _deductionResult.value = DeductionResult(false, "RELACIÓN DÉBIL: No hay una conexión lógica clara entre estos elementos.")
            }
        }
    }

    private fun updateSuspectTrust(suspectId: String, newTrust: Int) {
        _gameState.update { state ->
            val case = state.currentCase ?: return@update state
            val updatedSuspects = case.suspects.map {
                if (it.id == suspectId) it.copy(trustLevel = newTrust) else it
            }
            state.copy(currentCase = case.copy(suspects = updatedSuspects))
        }
    }

    private fun updateMessages(id: String, msg: InterrogationMessage) {
        val map = _messages.value.toMutableMap()
        map[id] = (map[id] ?: emptyList()) + msg
        _messages.value = map
    }

    fun submitAccusation(accusedId: String) {
        val case = _gameState.value.currentCase ?: return
        viewModelScope.launch {
            isSubmittingAccusation = true
            val isCorrect = accusedId == case.solution.guiltySuspectId
            val accused = case.suspects.find { it.id == accusedId }
            val guilty = case.suspects.find { it.id == case.solution.guiltySuspectId }
            
            val epilogue = groqService.generateEpilogue(case.title, case.description, guilty?.name ?: "", accused?.name ?: "", isCorrect)
            
            _gameResult.value = GameResult(
                isCorrect = isCorrect,
                accusedSuspectName = accused?.name ?: "",
                actualGuiltyName = guilty?.name ?: "",
                epilogue = epilogue,
                correctClueAssignments = 0,
                totalClues = case.clues.size
            )
            _gameState.update { it.copy(phase = GamePhase.VERDICT) }
            isSubmittingAccusation = false
        }
    }

    fun resetGame() {
        _gameState.value = GameState()
        _selectedClues.value = emptySet()
        _deductionResult.value = null
        _gameResult.value = null
        sharedPrefs.edit().remove("current_case").apply()
    }

    private fun getCurrentTime() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
