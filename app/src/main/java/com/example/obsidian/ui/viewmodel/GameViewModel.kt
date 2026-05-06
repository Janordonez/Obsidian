package com.example.obsidian.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.obsidian.R
import com.example.obsidian.data.model.*
import com.example.obsidian.data.remote.GeminiService
import com.example.obsidian.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)
    private val sharedPrefs = application.getSharedPreferences("obsidian_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _currentCase = MutableStateFlow<GameCase?>(null)
    val currentCase = _currentCase.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<InterrogationMessage>>>(emptyMap())
    val messages = _messages.asStateFlow()

    var isGenerating by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val maleImages = listOf(R.drawable.sus1, R.drawable.sus3, R.drawable.malesus1, R.drawable.malesus2, R.drawable.malesus3, R.drawable.malesus4)
    private val femaleImages = listOf(R.drawable.susfemale1, R.drawable.susfemale2, R.drawable.femalesus1, R.drawable.femalesus2, R.drawable.femalesus3, R.drawable.femalesus4)

    init {
        loadSavedCase()
    }

    private fun loadSavedCase() {
        val savedCaseJson = sharedPrefs.getString("current_case", null)
        if (savedCaseJson != null) {
            try {
                val savedCase = json.decodeFromString<GameCase>(savedCaseJson)
                _currentCase.value = savedCase
                
                // Load messages if any (could also be saved, but let's start with the case)
                val initialMessages = savedCase.suspects.associate { it.id to listOf(
                    InterrogationMessage("SISTEMA", "INTERROGATORIO REANUDADO", false, getCurrentTime())
                ) }
                _messages.value = initialMessages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveCase(gameCase: GameCase) {
        val caseJson = json.encodeToString(gameCase)
        sharedPrefs.edit().putString("current_case", caseJson).apply()
    }

    fun startNewInvestigation() {
        viewModelScope.launch {
            isGenerating = true
            errorMessage = null
            try {
                val newCase = geminiService.generateCase()
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
                    val initialMessages = mappedSuspects.associate { it.id to listOf(
                        InterrogationMessage("SISTEMA", "INTERROGATORIO INICIADO", false, getCurrentTime())
                    ) }
                    _messages.value = initialMessages
                } else {
                    errorMessage = "No se pudo generar el caso (Respuesta nula)."
                }
            } catch (e: Exception) {
                errorMessage = "Error de IA: ${e.message}"
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

            val response = geminiService.getSuspectResponse(
                suspect.name,
                suspect.personality,
                history,
                text,
                strategy
            )

            val aiMsg = InterrogationMessage(suspectId, response, false, getCurrentTime())
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

    private val _deductionAnalysis = MutableStateFlow("")
    val deductionAnalysis = _deductionAnalysis.asStateFlow()

    var isAnalyzing by mutableStateOf(false)
        private set

    fun analyzeDeductions(assignments: Map<String, List<String>>) {
        val currentCase = _currentCase.value ?: return
        viewModelScope.launch {
            isAnalyzing = true
            val analysis = geminiService.getDeductionAnalysis(
                currentCase.title,
                currentCase.description,
                assignments
            )
            _deductionAnalysis.value = analysis
            isAnalyzing = false
        }
    }
}
