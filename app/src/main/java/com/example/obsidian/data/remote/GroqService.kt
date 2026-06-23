package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.GameCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class GroqService(apiKey: String) {
    private val cleanApiKey = "Bearer ${apiKey.replace(" ", "").trim()}"
    private val json = Json { ignoreUnknownKeys = true }
    
    private val api: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GroqApi::class.java)
    }

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
            val request = GroqRequest(
                messages = listOf(GroqMessage("user", prompt)),
                response_format = GroqResponseFormat()
            )
            val response = api.getCompletion(cleanApiKey, request)
            val content = response.choices.firstOrNull()?.message?.content ?: return null
            json.decodeFromString<GameCase>(content.trim())
        } catch (e: Exception) {
            Log.e("GroqService", "Error: ${e.message}")
            null
        }
    }

    suspend fun getSuspectResponse(
        suspectName: String,
        personality: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): String {
        val historyPrompt = history.joinToString("\n") { "${it.first}: ${it.second}" }
        val systemPrompt = "Eres $suspectName, un sospechoso. Tu personalidad es $personality. Responde de forma breve en español. No uses asteriscos."
        
        val messages = mutableListOf<GroqMessage>()
        messages.add(GroqMessage("system", systemPrompt))
        history.forEach {
            messages.add(GroqMessage(if (it.first == "Detective") "user" else "assistant", it.second))
        }
        messages.add(GroqMessage("user", userMessage))

        return try {
            val request = GroqRequest(messages = messages, response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content ?: "..."
        } catch (e: Exception) {
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
            Analiza estas asignaciones para el caso "$caseTitle".
            Descripción: $caseDescription
            Asignaciones: ${assignments.entries.joinToString { "${it.key}: ${it.value}" }}
            Di si son correctas en texto plano, sin simbolos.
        """.trimIndent()

        return try {
            val request = GroqRequest(messages = listOf(GroqMessage("user", prompt)), response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content ?: "Error de analisis."
        } catch (e: Exception) {
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
            val request = GroqRequest(messages = listOf(GroqMessage("user", prompt)), response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content ?: "El caso ha sido archivado."
        } catch (e: Exception) {
            Log.e("GroqService", "Error en generateEpilogue: ${e.message}")
            "El caso ha sido archivado en los registros de OBSIDIAN."
        }
    }
}
