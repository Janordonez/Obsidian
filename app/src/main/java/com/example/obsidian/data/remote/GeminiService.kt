package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.GameCase
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.json.Json


class GeminiService(apiKey: String) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cleanApiKey = apiKey.replace(" ", "").replace("\n", "").trim()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = cleanApiKey,
    )

    suspend fun generateCase(suspectCount: Int = 4, clueCount: Int = 3): GameCase? {
        val prompt = """
            Genera un caso de misterio para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura:
            {
              "title": "Titulo",
              "description": "Descripcion breve del crimen",
              "guiltyId": "ID del sospechoso culpable (ej: S1)",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre Completo",
                  "gender": "MALE o FEMALE",
                  "personality": "Hostil/Evasivo/etc",
                  "background": "Historia breve",
                  "relation": "Relación con el caso (ej: Esposa de la víctima)",
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
                  "description": "Lo que se encuentra",
                  "ownerSuspectId": "ID del sospechoso al que incrimina (ej: S1)"
                }
              ]
            }
            Genera exactamente $suspectCount sospechosos (S1, S2, ..., S$suspectCount) y $clueCount pistas.
            El campo "guiltyId" DEBE ser el ID de uno de los $suspectCount sospechosos.
            Cada pista debe incriminar a un sospechoso mediante "ownerSuspectId".
            Al menos una pista debe incriminar al culpable (guiltyId).
            No incluyas alias. Solo devuelve el JSON, sin texto adicional.
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
        strategy: String,
    ): String {
        val chatModel = GenerativeModel(
            modelName = "gemini-2.5-flash-lite",
            apiKey = cleanApiKey,
        )

        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }

        val systemPrompt = """
            Eres $suspectName, un sospechoso en un juego.
            Tu personalidad es $personality.
            Tu estrategia de respuesta actual es: $strategy.
            
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
        assignments: Map<String, List<String>>,
        correctCount: Int,
        totalClues: Int
    ): String {
        val prompt = """
            Eres el sistema operativo OBSIDIAN.
            Analiza estas asignaciones del detective para el caso "$caseTitle".
            $caseDescription
            
            Asignaciones del detective:
            ${assignments.entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }}
            
            El detective ha asignado correctamente $correctCount de $totalClues pistas.
            
            Si todas son correctas, felicita brevemente al detective y dile que puede proceder a la acusacion.
            Si hay errores, da una pista sutil sin revelar la respuesta directa.
            IMPORTANTE: Responde solo con texto plano. No uses asteriscos, guiones al inicio, ni formato markdown.
            Responde en español. Maximo 3 oraciones.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Error en el análisis."
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en getDeductionAnalysis: ${e.message}")
            "Error de conexion."
        }
    }

    suspend fun generateEpilogue(
        caseTitle: String,
        caseDescription: String,
        guiltyName: String,
        accusedName: String,
        wasCorrect: Boolean
    ): String {
        val resultText = if (wasCorrect) {
            "El detective acuso correctamente a $accusedName, quien era el verdadero culpable."
        } else {
            "El detective acuso incorrectamente a $accusedName. El verdadero culpable era $guiltyName."
        }

        val prompt = """
            Eres el sistema operativo OBSIDIAN.
            Genera un epilogo narrativo breve para el caso "$caseTitle".
            Descripcion del caso: $caseDescription
            
            Resultado: $resultText
            
            Escribe un epilogo dramatico de 3-4 oraciones en español que cierre la historia.
            Si el detective acerto, haz que suene como una victoria satisfactoria.
            Si el detective fallo, haz que suene como una leccion aprendida.
            IMPORTANTE: Responde solo con texto plano. No uses asteriscos, guiones al inicio, ni formato markdown.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "El caso ha sido archivado."
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en generateEpilogue: ${e.message}")
            "El caso ha sido archivado en los registros de OBSIDIAN."
        }
    }
}