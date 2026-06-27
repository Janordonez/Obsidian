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
import com.example.obsidian.data.local.*
import com.example.obsidian.data.remote.GeminiService
import com.example.obsidian.data.remote.GroqService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val db = AppDatabase.getDatabase(application)
    private val gameSaveDao = db.gameSaveDao()
    
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

    private val _unlockedClueAlert = MutableStateFlow<Clue?>(null)
    val unlockedClueAlert = _unlockedClueAlert.asStateFlow()

    fun clearUnlockedClueAlert() {
        _unlockedClueAlert.value = null
    }

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

    var selectedInterrogationSuspectId by mutableStateOf<String?>(null)
        private set

    private var interrogationTimerJob: Job? = null
    private var activeInterrogationSuspectId: String? = null

    init { loadSavedCase() }

    private fun loadSavedCase() {
        viewModelScope.launch {
            try {
                val save = withContext(Dispatchers.IO) { gameSaveDao.getSave() }
                if (save != null) {
                    val savedState = json.decodeFromString<GameState>(save.serializedState)
                    _gameState.value = savedState
                    _messages.value = savedState.messages
                    _gameResult.value = savedState.gameResult
                    savedState.interrogationSessions.entries
                        .firstOrNull { it.value.isActive }
                        ?.let { startInterrogationTimer(it.key) }
                } else {
                    loadDefaultCase()
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error loading game state from Room", e)
                loadDefaultCase()
            }
        }
    }

    private fun loadDefaultCase() {
        val case = CaseRepository.laCargaDelSilencio
        val initialState = GameState(
            currentCase = case,
            phase = GamePhase.INTRO
        )
        _gameState.value = initialState
        _messages.value = case.suspects.associate { it.id to listOf(
            InterrogationMessage("SISTEMA", "EXPEDIENTE INICIADO", false, getCurrentTime())
        ) }
        saveGameState(initialState)
    }

    private fun saveGameState(state: GameState) {
        viewModelScope.launch {
            try {
                val serialized = json.encodeToString(state)
                withContext(Dispatchers.IO) {
                    gameSaveDao.insertSave(GameSaveEntity(serialized))
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error saving game state to Room", e)
            }
        }
    }

    fun startNewInvestigation() {
        _selectedClues.value = emptySet()
        _deductionResult.value = null
        _gameResult.value = null
        loadDefaultCase()
    }

    fun exploreScene(locationId: String) {
        val newState = GameEngine.exploreLocation(_gameState.value, locationId)
        updateGameState(newState)
    }

    fun collectClue(clueId: String) {
        val newState = GameEngine.exploreLocation(_gameState.value, clueId)
        updateGameState(newState)
    }

    fun applyMinigameResult(locationName: String, result: String) {
        val newState = GameEngine.applyMinigameResult(_gameState.value, locationName, result)
        updateGameState(newState)
    }

    fun startInvestigation() {
        val newState = GameEngine.navigateToPhase(_gameState.value, GamePhase.EXPLORE_SCENE)
        updateGameState(newState)
    }

    fun linkClueToSuspect(clueId: String, suspectId: String?) {
        val currentSuspectId = suspectId ?: ""
        val newState = GameEngine.linkClueToSuspect(_gameState.value, clueId, currentSuspectId)
        updateGameState(newState)
    }

    private fun updateGameState(newState: GameState) {
        val oldAvailableClueIds = _gameState.value.currentCase?.clues?.filter { it.isAvailable }?.map { it.id }?.toSet() ?: emptySet()
        val newAvailableClues = newState.currentCase?.clues?.filter { it.isAvailable } ?: emptyList()
        val newlyUnlocked = newAvailableClues.filter { it.id !in oldAvailableClueIds }
        
        _gameState.value = newState
        _messages.value = newState.messages
        _gameResult.value = newState.gameResult
        saveGameState(newState)
        
        if (oldAvailableClueIds.isNotEmpty() && newlyUnlocked.isNotEmpty()) {
            newlyUnlocked.forEach { clue ->
                _unlockedClueAlert.value = clue
            }
        }
    }

    fun selectInterrogationSuspect(suspectId: String) {
        selectedInterrogationSuspectId = suspectId
    }

    fun onQuestionSelected(suspect: Suspect, question: Question) {
        val newState = GameEngine.askQuestion(_gameState.value, suspect.id, question.id)
        updateGameState(newState)
    }

    fun startInterrogation(suspectId: String) {
        stopInterrogationTimer()
        val newState = GameEngine.startInterrogation(_gameState.value, suspectId)
        updateGameState(newState)
        activeInterrogationSuspectId = suspectId
        startInterrogationTimer(suspectId)
    }

    fun endInterrogation(suspectId: String) {
        stopInterrogationTimer()
        val newState = GameEngine.endInterrogation(_gameState.value, suspectId)
        updateGameState(newState)
        activeInterrogationSuspectId = null
    }

    fun getQuestionsForRound(suspectId: String): List<Question> {
        return GameEngine.getQuestionsForRound(_gameState.value, suspectId)
    }

    fun isInterrogationActive(suspectId: String): Boolean {
        val session = _gameState.value.interrogationSessions[suspectId]
        return session?.isActive == true
    }

    fun isInterrogationCompleted(suspectId: String): Boolean {
        return suspectId in _gameState.value.interrogatedSuspects
    }

    fun getInterrogationSummary(suspectId: String): InterrogationSummary? {
        return _gameState.value.interrogationSummaries[suspectId]
    }

    fun getInterrogationTimeRemaining(suspectId: String): Int {
        return _gameState.value.interrogationSessions[suspectId]?.timeRemainingSeconds ?: 0
    }

    private fun startInterrogationTimer(suspectId: String) {
        interrogationTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _gameState.value
                val session = state.interrogationSessions[suspectId]
                if (session == null || !session.isActive) break

                val newState = if (session.timeRemainingSeconds <= 1) {
                    GameEngine.handleTimeExpired(state, suspectId)
                } else {
                    GameEngine.tickInterrogationTime(state, suspectId)
                }
                updateGameState(newState)
            }
        }
    }

    private fun stopInterrogationTimer() {
        interrogationTimerJob?.cancel()
        interrogationTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopInterrogationTimer()
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
                val updatedSuspects = case.suspects.map {
                    if (it.id == suspectId) it.copy(trustLevel = (it.trustLevel - 15).coerceIn(0, 100)) else it
                }
                updateGameState(_gameState.value.copy(currentCase = case.copy(suspects = updatedSuspects)))
            } else {
                _deductionResult.value = DeductionResult(false, "RELACIÓN DÉBIL: No hay una conexión lógica clara entre estos elementos.")
            }
        }
    }

    private fun updateMessages(id: String, msg: InterrogationMessage) {
        val map = _messages.value.toMutableMap()
        map[id] = (map[id] ?: emptyList()) + msg
        _messages.value = map
        
        _gameState.update { it.copy(messages = map) }
        saveGameState(_gameState.value)
    }

    fun confrontStatementWithClue(statementId: String, clueId: String) {
        val newState = GameEngine.confrontStatementWithClue(_gameState.value, statementId, clueId)
        updateGameState(newState)
    }

    fun compareStatements(statementId1: String, statementId2: String) {
        val newState = GameEngine.compareStatements(_gameState.value, statementId1, statementId2)
        updateGameState(newState)
    }

    fun pressSuspect(suspectId: String) {
        val newState = GameEngine.pressSuspect(_gameState.value, suspectId)
        updateGameState(newState)
    }

    fun submitAccusation(accusedId: String, keyEvidenceId: String) {
        isSubmittingAccusation = true
        val updatedState = GameEngine.submitAccusation(_gameState.value, accusedId, keyEvidenceId)
        updateGameState(updatedState)
        isSubmittingAccusation = false
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

    fun resetGame() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    gameSaveDao.deleteSave()
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error deleting game save from Room", e)
            }
        }
        _gameState.value = GameState()
        _selectedClues.value = emptySet()
        _deductionResult.value = null
        _gameResult.value = null
        sharedPrefs.edit().remove("current_case").apply()
    }

    private fun getCurrentTime() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
