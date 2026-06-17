package com.example.obsidian.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.obsidian.BuildConfig
import com.example.obsidian.R
import com.example.obsidian.data.model.AISuspect
import com.example.obsidian.data.model.GameCase
import com.example.obsidian.data.model.GameResult
import com.example.obsidian.data.model.InterrogationMessage
import com.example.obsidian.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.example.obsidian.data.remote.GroqService

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)
    private val groqService = GroqService(BuildConfig.GROQ_API_KEY)
    private val sharedPrefs = application.getSharedPreferences("obsidian_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _currentCase = MutableStateFlow<GameCase?>(null)
    val currentCase = _currentCase.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<InterrogationMessage>>>(emptyMap())
    val messages = _messages.asStateFlow()

    var isGenerating by mutableStateOf(value = false)
        private set

    var errorMessage by mutableStateOf<String?>(value = null)
        private set

    // Clue assignments state
    private val _clueAssignments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val clueAssignments = _clueAssignments.asStateFlow()

    // Game result state
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult = _gameResult.asStateFlow()

    private val _deductionAnalysis = MutableStateFlow("")
    val deductionAnalysis = _deductionAnalysis.asStateFlow()

    var isAnalyzing by mutableStateOf(value = false)
        private set

    var isSubmittingAccusation by mutableStateOf(value = false)
        private set

    // Audio Settings
    var musicVolume by mutableFloatStateOf(0.7f)
    var effectsVolume by mutableFloatStateOf(0.8f)
    var isMusicEnabled by mutableStateOf(value = true)
    var isEffectsEnabled by mutableStateOf(value = true)

    private val maleImages = listOf(
        R.drawable.sus1,
        R.drawable.sus3,
        R.drawable.malesus1,
        R.drawable.malesus2,
        R.drawable.malesus3,
        R.drawable.malesus4,
    )
    private val femaleImages = listOf(
        R.drawable.susfemale1,
        R.drawable.susfemale2,
        R.drawable.femalesus1,
        R.drawable.femalesus2,
        R.drawable.femalesus3,
        R.drawable.femalesus4,
    )

    init {
        loadSavedCase()
    }

    private fun loadSavedCase() {
        val savedCaseJson = sharedPrefs.getString("current_case", null)
        if (savedCaseJson != null) {
            try {
                val savedCase = json.decodeFromString<GameCase>(savedCaseJson)
                _currentCase.value = savedCase

                val initialMessages = savedCase.suspects.associateBy(
                    keySelector = { it.id }
                ) {
                    listOf(
                        InterrogationMessage("SISTEMA", "INTERROGATORIO REANUDADO", isDetective = false, getCurrentTime())
                    )
                }
                _messages.value = initialMessages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveCase(gameCase: GameCase) {
        val caseJson = json.encodeToString(gameCase)
        sharedPrefs.edit { putString("current_case", caseJson) }
    }

    fun startNewInvestigation() {
        viewModelScope.launch {
            isGenerating = true
            errorMessage = null
            _gameResult.value = null
            _clueAssignments.value = emptyMap()
            _deductionAnalysis.value = ""
            try {
                val suspectCount = (3..5).random()
                val clueCount = (4..10).random()
                
                var newCase: GameCase? = null
                try {
                    Log.d("GameViewModel", "Intentando generar caso con Gemini ($suspectCount sospechosos, $clueCount pistas)...")
                    newCase = geminiService.generateCase(suspectCount, clueCount)
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error al generar caso con Gemini: ${e.message}")
                }

                if (newCase == null) {
                    Log.d("GameViewModel", "Gemini falló o retornó nulo, intentando backup con Groq...")
                    newCase = groqService.generateCase(suspectCount, clueCount)
                }

                if (newCase != null) {
                    var maleIdx = 0
                    var femaleIdx = 0
                    
                    val mappedSuspects = newCase.suspects.map { suspect ->
                        val imageId = if (suspect.gender.uppercase() == "FEMALE") {
                            femaleImages.getOrElse(femaleIdx++) { femaleImages[0] }
                        } else {
                            maleImages.getOrElse(maleIdx++) { maleImages[0] }
                        }
                        suspect.copy(imageId = imageId)
                    }
                    val finalCase = newCase.copy(suspects = mappedSuspects)
                    _currentCase.value = finalCase
                    saveCase(finalCase)
                    
                    // Initialize messages
                    val initialMessages = mappedSuspects.associateBy(
                        keySelector = { it.id },
                        valueTransform = {
                            listOf(
                                InterrogationMessage("SISTEMA", "INTERROGATORIO INICIADO", false, getCurrentTime())
                            )
                        }
                    )
                    _messages.value = initialMessages
                } else {
                    errorMessage = "No se pudo generar el caso con Gemini ni con Groq (backup)."
                }
            } catch (e: Exception) {
                errorMessage = "Error de IA (ambos proveedores fallaron): ${e.message}"
            }
            isGenerating = false
        }
    }

    fun sendMessage(suspect: AISuspect, text: String, strategy: String) {
        val suspectId = suspect.id
        val currentTime = getCurrentTime()
        
        // Add user message
        val userMsg = InterrogationMessage("INVESTIGADOR", text, true, currentTime)
        updateMessages(suspectId, userMsg)

        viewModelScope.launch {
            val history = _messages.value[suspectId]
                ?.filter { !it.text.startsWith("INTERROGATORIO") }
                ?.takeLast(5)
                ?.map { (if (it.isDetective) "Detective" else suspect.name) to it.text }
                ?: emptyList()

            var response: String? = null
            try {
                Log.d("GameViewModel", "Intentando obtener respuesta de sospechoso con Gemini...")
                response = geminiService.getSuspectResponse(
                    suspect.name,
                    suspect.personality,
                    history,
                    text,
                    strategy
                )
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error en Gemini: ${e.message}")
            }

            if ((response == null) || (response == "...")) {
                Log.d("GameViewModel", "Intentando backup con Groq para respuesta del sospechoso...")
                try {
                    response = groqService.getSuspectResponse(
                        suspect.name,
                        suspect.personality,
                        history,
                        text
                    )
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error en Groq (backup): ${e.message}")
                }
            }

            val finalResponse = response ?: "..."
            val aiMsg = InterrogationMessage(suspectId, finalResponse, false, getCurrentTime())
            updateMessages(suspectId, aiMsg)
        }
    }

    private fun updateMessages(suspectId: String, message: InterrogationMessage) {
        val currentMap = _messages.value.toMutableMap()
        val suspectMessages = currentMap[suspectId]?.toMutableList() ?: mutableListOf()
        suspectMessages.add(message)
        currentMap[suspectId] = suspectMessages
        _messages.value = currentMap
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // ---- Clue Assignment Functions ----

    fun assignClueToSuspect(suspectName: String, clueTitle: String) {
        val updatedMap = _clueAssignments.value.toMutableMap()
        
        // Remove clue from any other suspect first
        _clueAssignments.value.forEach { (name, list) ->
            updatedMap[name] = list.filterNot { it == clueTitle }
        }

        val currentClues = updatedMap[suspectName] ?: emptyList()
        updatedMap[suspectName] = (currentClues + clueTitle).distinct()

        _clueAssignments.value = updatedMap
    }

    fun getCorrectClueCount(): Int {
        val gameCase = _currentCase.value ?: return 0
        val assignments = _clueAssignments.value
        
        var correctCount = 0
        gameCase.clues.forEach { clue ->
            val ownerSuspect = gameCase.suspects.find { it.id == clue.ownerSuspectId }
            val assignedToName = assignments.entries.find { entry ->
                clue.title in entry.value
            }?.key
            if ((ownerSuspect != null) && (assignedToName == ownerSuspect.name)) {
                correctCount++
            }
        }
        return correctCount
    }

    fun allCluesAssigned(): Boolean {
        val currentCase = _currentCase.value ?: return false
        val allClues = currentCase.clues.map { it.title }
        val assignedClues = _clueAssignments.value.values.asSequence().flatten().toSet()
        return allClues.all { it in assignedClues }
    }

    fun analyzeDeductions(assignments: Map<String, List<String>>) {
        val currentCase = _currentCase.value ?: return
        viewModelScope.launch {
            isAnalyzing = true
            val correctCount = getCorrectClueCount()
            
            var analysis: String? = null
            try {
                Log.d("GameViewModel", "Intentando analizar deducciones con Gemini...")
                analysis = geminiService.getDeductionAnalysis(
                    currentCase.title,
                    currentCase.description,
                    assignments,
                    correctCount,
                    currentCase.clues.size
                )
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al analizar deducciones con Gemini: ${e.message}")
            }

            if (analysis == null || analysis.startsWith("Error")) {
                Log.d("GameViewModel", "Intentando backup con Groq para análisis de deducciones...")
                try {
                    analysis = groqService.getDeductionAnalysis(
                        currentCase.title,
                        currentCase.description,
                        assignments
                    )
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error en Groq (backup) al analizar deducciones: ${e.message}")
                }
            }

            _deductionAnalysis.value = analysis ?: "Error al analizar las deducciones."
            isAnalyzing = false
        }
    }

    fun submitAccusation(accusedSuspectId: String) {
        val gameCase = _currentCase.value ?: return
        viewModelScope.launch {
            isSubmittingAccusation = true
            errorMessage = null

            val accusedSuspect = gameCase.suspects.find { it.id == accusedSuspectId }
            val guiltySuspect = gameCase.suspects.find { it.id == gameCase.guiltyId }

            val isCorrect = accusedSuspectId == gameCase.guiltyId
            val correctClues = getCorrectClueCount()

            var epilogue: String? = null
            try {
                Log.d("GameViewModel", "Intentando generar epílogo con Gemini...")
                epilogue = geminiService.generateEpilogue(
                    caseTitle = gameCase.title,
                    caseDescription = gameCase.description,
                    guiltyName = guiltySuspect?.name ?: "Desconocido",
                    accusedName = accusedSuspect?.name ?: "Desconocido",
                    wasCorrect = isCorrect
                )
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error en epílogo con Gemini: ${e.message}")
            }

            if (epilogue == null || epilogue.startsWith("Error") || epilogue.contains("archivado")) {
                Log.d("GameViewModel", "Intentando backup con Groq para generar epílogo...")
                try {
                    epilogue = groqService.generateEpilogue(
                        caseTitle = gameCase.title,
                        caseDescription = gameCase.description,
                        guiltyName = guiltySuspect?.name ?: "Desconocido",
                        accusedName = accusedSuspect?.name ?: "Desconocido",
                        wasCorrect = isCorrect
                    )
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error en Groq (backup) para epílogo: ${e.message}")
                }
            }

            _gameResult.value = GameResult(
                isCorrect = isCorrect,
                accusedSuspectName = accusedSuspect?.name ?: "Desconocido",
                actualGuiltyName = guiltySuspect?.name ?: "Desconocido",
                epilogue = epilogue ?: "El caso ha sido archivado en los registros de OBSIDIAN.",
                correctClueAssignments = correctClues,
                totalClues = gameCase.clues.size
            )

            isSubmittingAccusation = false
        }
    }

    fun resetGame() {
        _currentCase.value = null
        _messages.value = emptyMap()
        _gameResult.value = null
        _clueAssignments.value = emptyMap()
        _deductionAnalysis.value = ""
        errorMessage = null
        sharedPrefs.edit { remove("current_case") }
    }
}


