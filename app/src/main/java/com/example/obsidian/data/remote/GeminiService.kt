package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.json.Json

class GeminiService(apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cleanApiKey = apiKey.replace(" ", "").replace("\n", "").trim()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = cleanApiKey,
    )

    suspend fun generateCase(suspectCount: Int = 3): Case? {
        val prompt = """
            Genera un caso de misterio complejo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura:
            {
              "id": "CASE-" + numero aleatorio,
              "title": "Titulo",
              "description": "Descripcion",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre",
                  "personality": "Rasgos",
                  "alibi": "Coartada",
                  "contradictions": ["Contradiccion"],
                  "trustLevel": 50
                }
              ],
              "clues": [
                {
                  "id": "C1",
                  "title": "Pista",
                  "description": "Detalle",
                  "linkedSuspects": ["S1"],
                  "importance": 5,
                  "unlockConditionType": "INTERROGATION",
                  "unlockConditionValue": "S1",
                  "isAvailable": false
                }
              ],
              "solution": {
                "guiltySuspectId": "S1",
                "motive": "Motivo",
                "keyEvidenceId": "C1"
              }
            }
            Reglas de Desbloqueo:
            - "unlockConditionType": "START" (desde el inicio), "INTERROGATION" (hablar con sospechoso), "EXPLORATION" (inspeccionar N veces), "CLUE" (tener otra pista).
            - "unlockConditionValue": ID de sospechoso, número de veces, o ID de pista.
            - Al menos 1 pista debe ser "START".
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            var cleanText = response.text ?: return null
            if (cleanText.contains("```json")) cleanText = cleanText.substringAfter("```json").substringBefore("```")
            json.decodeFromString<Case>(cleanText.trim())
        } catch (e: Exception) {
            Log.e("GeminiService", "Error: ${e.message}")
            null
        }
    }

    suspend fun getSuspectResponse(suspect: Suspect, history: List<Pair<String, String>>, userMessage: String): String {
        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }
        val systemPrompt = "Eres ${suspect.name}. Personalidad: ${suspect.personality}. Coartada: ${suspect.alibi}. Trust: ${suspect.trustLevel}/100. Responde breve y natural en español."
        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\n$historyPrompt\nDetective: $userMessage")
            response.text ?: "..."
        } catch (e: Exception) { "..." }
    }

    suspend fun generateEpilogue(title: String, desc: String, guilty: String, accused: String, correct: Boolean): String {
        val prompt = "Genera un epilogo dramático para el caso $title. Culpable real: $guilty. Acusado: $accused. Resultado: ${if(correct) "ACERTO" else "FALLO"}. Responde en 3 oraciones en español."
        return try { generativeModel.generateContent(prompt).text ?: "Caso cerrado." } catch (e: Exception) { "Caso cerrado." }
    }
}
