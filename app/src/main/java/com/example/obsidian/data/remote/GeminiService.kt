package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.GameCase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.serialization.json.Json

class GeminiService(apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cleanApiKey = apiKey.replace(" ", "").replace("\n", "").trim()

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = cleanApiKey,
        safetySettings = safetySettings
    )

    suspend fun generateCase(): GameCase? {
        val prompt = """
            Genera un caso de misterio sencillo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura:
            {
              "title": "Titulo",
              "description": "Descripcion breve del crimen",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre Completo",
                  "alias": "Alias",
                  "personality": "Hostil/Evasivo/etc",
                  "background": "Historia breve",
                  "alibi": "Donde estaba",
                  "tension": 0.5,
                  "status": "NORMAL",
                  "bpm": 80,
                  "caseNumber": "#001",
                  "room": "SALA A"
                }
              ],
              "clues": [
                {
                  "id": 1,
                  "title": "Nombre pista",
                  "locationName": "Lugar",
                  "latitude": 34.05,
                  "longitude": -118.24,
                  "description": "Lo que se encuentra"
                }
              ]
            }
            Genera exactamente 4 sospechosos y 3 pistas. Usa lenguaje apto para todo publico.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            var cleanText = response.text ?: throw Exception("IA: Respuesta vacía.")
            Log.d("GeminiService", "Respuesta recibida: ${cleanText.take(100)}...")
            
            if (cleanText.contains("```json")) {
                cleanText = cleanText.substringAfter("```json").substringBefore("```")
            } else if (cleanText.contains("```")) {
                cleanText = cleanText.substringAfter("```").substringBefore("```")
            }
            
            json.decodeFromString<GameCase>(cleanText.trim())
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en generateCase: ${e.message}")
            throw e
        }
    }

    suspend fun getSuspectResponse(
        suspectName: String,
        personality: String,
        history: List<Pair<String, String>>,
        userMessage: String,
        strategy: String
    ): String {
        val chatModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = cleanApiKey,
            safetySettings = safetySettings
        )
        
        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }
        
        val systemPrompt = """
            Eres $suspectName, un sospechoso en un juego.
            Tu personalidad es $personality.
            Conversacion previa:
            $historyPrompt
            
            Detective dice: "$userMessage"
            
            Responde como el personaje de forma breve en español.
        """.trimIndent()

        return try {
            val response = chatModel.generateContent(systemPrompt)
            response.text ?: "..."
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en getSuspectResponse: ${e.message}")
            "..."
        }
    }

    suspend fun getDeductionAnalysis(
        caseTitle: String,
        caseDescription: String,
        assignments: Map<String, List<String>>
    ): String {
        val prompt = """
            Eres el sistema operativo OBSIDIAN.
            Analiza estas asignaciones del detective para el caso "$caseTitle".
            $caseDescription
            
            Asignaciones:
            ${assignments.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }}
            
            Di si las deducciones son correctas o da una pista en español.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Error en el análisis."
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en getDeductionAnalysis: ${e.message}")
            "Error de conexion."
        }
    }
}
