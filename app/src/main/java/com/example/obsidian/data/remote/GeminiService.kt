package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.json.Json

class GeminiService(apiKey: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val cleanApiKey = apiKey.replace(" ", "").replace("\n", "").trim()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = cleanApiKey,
    )

    suspend fun generateCase(suspectCount: Int = 3): Case? {
        val prompt = """
            Genera un caso de misterio complejo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura exacta sin texto complementario ni markdown:
            {
              "id": "CASE-1234",
              "title": "Título del Caso",
              "description": "Descripción detallada del crimen",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre del Sospechoso",
                  "gender": "MALE",
                  "personality": "Rasgos de personalidad",
                  "alibi": "Coartada inicial",
                  "contradictions": ["Contradicción oculta"],
                  "trustLevel": 50,
                  "relation": "Relación con la víctima",
                  "room": "Ubicación inicial",
                  "availableQuestions": [
                    {
                      "id": "Q1",
                      "text": "Pregunta de ejemplo",
                      "effectType": "TRUST",
                      "effectValue": "-15",
                      "coherence": 80,
                      "topic": "Coartada",
                      "approach": "DIRECTO"
                    }
                  ]
                }
              ],
              "clues": [
                {
                  "id": "C1",
                  "title": "Nombre de la pista",
                  "description": "Detalle de lo que revela",
                  "linkedSuspects": ["S1"],
                  "importance": 5,
                  "locationName": "Laboratorio",
                  "latitude": 10.98,
                  "longitude": -74.78,
                  "isAvailable": false,
                  "isFound": false,
                  "unlockConditionType": "INTERROGATION",
                  "unlockConditionValue": "S1"
                }
              ],
              "solution": {
                "guiltySuspectId": "S1",
                "motive": "Motivo del crimen",
                "keyEvidenceId": "C1"
              }
            }

            Reglas obligatorias de consistencia:
            - "coherence": Puntuación entera de 0 a 100.
            - "topic": Tema de la pregunta.
            - "approach": DEBE SER una de estas 4 opciones en mayúsculas: "EMPÁTICO", "DIRECTO", "PRESIÓN", "TÉCNICO". No uses la palabra "focus".
            
            Reglas de Desbloqueo de Pistas:
            - "unlockConditionType" debe ser: "START", "INTERROGATION", "EXPLORATION", or "CLUE".
            - Al menos una de las pistas debe tener "unlockConditionType": "START" e "isAvailable": true.

            Genera exactamente $suspectCount sospechosos. No incluyas explicaciones antes o después del JSON.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val cleanText = cleanJsonString(response.text ?: return null)
            json.decodeFromString<Case>(cleanText)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error decodificando caso: ${e.message}")
            null
        }
    }

    suspend fun getSuspectResponse(suspect: Suspect, history: List<Pair<String, String>>, userMessage: String): String {
        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }
        val systemPrompt = "Eres ${suspect.name}. Personalidad: ${suspect.personality}. Coartada: ${suspect.alibi}. Confianza actual: ${suspect.trustLevel}/100. Responde breve y natural en español, acorde a tu rol."
        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\n$historyPrompt\nDetective: $userMessage")
            response.text?.trim() ?: "..."
        } catch (e: Exception) { "..." }
    }

    suspend fun generateEpilogue(title: String, desc: String, guilty: String, accused: String, correct: Boolean): String {
        val prompt = "Genera un epílogo dramático para el caso $title. Descripción: $desc. Culpable real: $guilty. Acusado: $accused. Resultado: ${if(correct) "ACERTÓ" else "FALLÓ"}. Escribe un párrafo de 3 oraciones en español sin markdown."
        return try { generativeModel.generateContent(prompt).text?.trim() ?: "Caso cerrado." } catch (e: Exception) { "Caso cerrado." }
    }

    suspend fun getDynamicInterrogationResponse(
        suspectId: String,
        questionId: String,
        trustLevel: Int,
        discoveredClueIds: List<String>
    ): InterrogationResponse? {
        val systemPrompt = """
            Eres el motor de interrogatorios del software de investigación Obsidian.
            Devuelve un JSON estrictamente con este formato:
            {
              "dialog": "Respuesta en primera persona del sospechoso.",
              "effect": {
                "type": "TRUST",
                "value": "-10"
              },
              "narratorNote": "Descripción del lenguaje corporal del sospechoso en tercera persona."
            }
            El campo effect.type solo puede ser: TRUST, CONTRADICTION, UNLOCK_CLUE o NONE.
        """.trimIndent()

        val contextPrompt = """
            SOSPECHOSO: $suspectId
            PREGUNTA: $questionId
            CONFIANZA: $trustLevel
            PISTAS EN PODER DEL DETECTIVE: ${discoveredClueIds.joinToString(", ")}
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\n$contextPrompt")
            val cleanText = cleanJsonString(response.text ?: return null)
            json.decodeFromString<InterrogationResponse>(cleanText)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error en interrogatorio dinámico: ${e.message}")
            null
        }
    }

    private fun cleanJsonString(rawText: String): String {
        var output = rawText.trim()
        if (output.contains("```json")) {
            output = output.substringAfter("```json").substringBefore("```")
        } else if (output.contains("```")) {
            output = output.substringAfter("```").substringBefore("```")
        }
        return output.trim()
    }
}

@kotlinx.serialization.Serializable
data class InterrogationEffect(
    val type: String,
    val value: String
)

@kotlinx.serialization.Serializable
data class InterrogationResponse(
    val dialog: String,
    val effect: InterrogationEffect,
    val narratorNote: String
)