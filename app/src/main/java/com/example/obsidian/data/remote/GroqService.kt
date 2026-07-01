package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class GroqService(apiKey: String) {
    private val cleanApiKey = "Bearer ${apiKey.replace(" ", "").trim()}"
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val api: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GroqApi::class.java)
    }

    suspend fun generateCase(suspectCount: Int = 3): Case? {
        val prompt = """
            Genera un caso de misterio complejo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura exacta sin texto adicional:
            {
              "id": "CASE-5678",
              "title": "Nombre del Caso",
              "description": "Descripción del crimen",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre Sospechoso",
                  "gender": "FEMALE",
                  "personality": "Fría",
                  "alibi": "Estaba en su despacho",
                  "contradictions": ["Dijo que no tenía acceso a las llaves"],
                  "trustLevel": 50,
                  "relation": "Contadora",
                  "room": "Oficina Central",
                  "availableQuestions": [
                    {
                      "id": "Q1",
                      "text": "¿Por qué alteró el balance?",
                      "effectType": "TRUST",
                      "effectValue": "-10",
                      "coherence": 90,
                      "topic": "Finanzas",
                      "approach": "TÉCNICO"
                    }
                  ]
                }
              ],
              "clues": [
                {
                  "id": "C1",
                  "title": "Libro contable",
                  "description": "Muestra desvíos de fondos",
                  "linkedSuspects": ["S1"],
                  "importance": 4,
                  "locationName": "Archivo",
                  "latitude": 10.95,
                  "longitude": -74.76,
                  "isAvailable": true,
                  "isFound": false,
                  "unlockConditionType": "START",
                  "unlockConditionValue": ""
                }
              ],
              "solution": {
                "guiltySuspectId": "S1",
                "motive": "Codicia",
                "keyEvidenceId": "C1"
              }
            }

            Reglas estructurales para "availableQuestions":
            - "coherence": Entero de 0 a 100.
            - "topic": Hilo temático de la pregunta.
            - "approach": Tono de aproximación del detective. DEBE SER estrictamente uno de los siguientes valores: "EMPÁTICO", "DIRECTO", "PRESIÓN", "TÉCNICO". No uses el término "focus".

            Genera exactamente $suspectCount sospechosos y al menos 5 pistas bien distribuidas. Atiende a que al menos una pista empiece disponible en "START".
        """.trimIndent()

        return try {
            val request = GroqRequest(
                messages = listOf(GroqMessage("user", prompt)),
                response_format = GroqResponseFormat()
            )
            val response = api.getCompletion(cleanApiKey, request)
            val content = response.choices.firstOrNull()?.message?.content ?: return null
            json.decodeFromString<Case>(cleanJsonString(content))
        } catch (e: Exception) {
            Log.e("GroqService", "Error en generateCase: ${e.message}")
            null
        }
    }

    suspend fun getSuspectResponse(
        suspect: Suspect,
        history: List<Pair<String, String>>,
        userMessage: String
    ): String {
        val systemPrompt = """
            Eres ${suspect.name}. Tu personalidad es ${suspect.personality}. 
            Tu coartada es: ${suspect.alibi}. Nivel de confianza actual: ${suspect.trustLevel}/100.
            Si tu confianza es baja (menor a 40), muéstrate a la defensiva o evasivo.
            Responde de forma concisa y natural en español sin usar asteriscos ni negritas.
        """.trimIndent()

        val messages = mutableListOf<GroqMessage>().apply {
            add(GroqMessage("system", systemPrompt))
            history.forEach { (sender, message) ->
                add(GroqMessage(if (sender == "Detective") "user" else "assistant", message))
            }
            add(GroqMessage("user", userMessage))
        }

        return try {
            val request = GroqRequest(messages = messages, response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content?.trim() ?: "..."
        } catch (e: Exception) {
            "..."
        }
    }

    suspend fun analyzeClueConnection(
        caseTitle: String,
        suspectName: String,
        clues: List<String>
    ): String {
        val prompt = """
            Eres el sistema de análisis Obsidian.
            Caso: $caseTitle
            Sospechoso: $suspectName
            Pistas conectadas: ${clues.joinToString(", ")}
            
            Escribe una deducción técnica concreta de un párrafo (máximo 2 oraciones) en español que vincule estas pistas con el sospechoso. Sé directo, no uses markdown.
        """.trimIndent()

        return try {
            val request = GroqRequest(messages = listOf(GroqMessage("user", prompt)), response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content?.trim() ?: "Vínculo analítico establecido con $suspectName."
        } catch (e: Exception) {
            "Deducción procesada: El vínculo con $suspectName es evidente."
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
            "El detective acusó con éxito a $accusedName, capturando al verdadero culpable."
        } else {
            "El detective acusó erróneamente a $accusedName. El verdadero responsable era $guiltyName."
        }

        val prompt = """
            Eres la terminal narrativa de OBSIDIAN.
            Genera un epílogo cinemático y dramático para el caso "$caseTitle".
            Sinopsis del caso: $caseDescription
            Resultado del juicio: $resultText
            
            Escribe 3-4 oraciones en español que cierren los hilos de la historia de forma profesional. Devuelve solo texto plano sin markdown.
        """.trimIndent()

        return try {
            val request = GroqRequest(messages = listOf(GroqMessage("user", prompt)), response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content?.trim() ?: "El caso ha sido archivado."
        } catch (e: Exception) {
            "El caso ha sido archivado en los registros de OBSIDIAN."
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