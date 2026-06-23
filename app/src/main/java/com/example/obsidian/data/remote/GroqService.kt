package com.example.obsidian.data.remote

import android.util.Log
import com.example.obsidian.data.model.Case
import com.example.obsidian.data.model.Suspect
import com.example.obsidian.data.model.Clue
import com.example.obsidian.data.model.Solution
import com.example.obsidian.data.model.Question
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

    suspend fun generateCase(suspectCount: Int = 3): Case? {
        val prompt = """
            Genera un caso de misterio complejo para un juego de detectives.
            Devuelve un JSON estrictamente con esta estructura:
            {
              "id": "CASE-" + numero aleatorio,
              "title": "Titulo del caso",
              "description": "Descripcion detallada del crimen",
              "suspects": [
                {
                  "id": "S1",
                  "name": "Nombre Completo",
                  "gender": "MALE/FEMALE",
                  "personality": "Rasgos de personalidad",
                  "alibi": "Coartada inicial",
                  "contradictions": ["Dato especifico que contradice su coartada"],
                  "trustLevel": 50,
                  "relation": "Relacion con victima",
                  "room": "Ubicacion",
                  "availableQuestions": [
                    {
                      "id": "Q1",
                      "text": "Pregunta inquisitiva sobre su coartada",
                      "effectType": "TRUST",
                      "effectValue": "-15"
                    },
                    {
                      "id": "Q2",
                      "text": "Pregunta para ganarse su confianza",
                      "effectType": "TRUST",
                      "effectValue": "10"
                    }
                  ]
                }
              ],
              "clues": [
                {
                  "id": "C1",
                  "title": "Nombre de la pista",
                  "description": "Descripcion de lo que revela la pista",
                  "linkedSuspects": ["S1"],
                  "importance": 5,
                  "locationName": "Nombre lugar",
                  "latitude": 34.05,
                  "longitude": -118.24,
                  "isAvailable": false,
                  "isFound": false,
                  "unlockConditionType": "INTERROGATION",
                  "unlockConditionValue": "S1"
                }
              ],
              "solution": {
                "guiltySuspectId": "ID del culpable",
                "motive": "Motivo del crimen",
                "keyEvidenceId": "ID de pista clave"
              }
            }
            Reglas del Interrogatorio:
            - "availableQuestions" son opciones de diálogo predefinidas que el jugador puede elegir.
            - "effectType" puede ser: "TRUST" (cambia trustLevel), "UNLOCK_CLUE" (hace disponible una pista), "CONTRADICTION" (añade una nueva contradicción revelada).
            - "effectValue": valor numérico (ej: "-20") para TRUST, ID de pista para UNLOCK_CLUE, o texto para CONTRADICTION.
            
            Reglas del Clue System:
            - "unlockConditionType" puede ser: "START" (disponible desde el inicio), "INTERROGATION" (se desbloquea al hablar con un sospechoso), "EXPLORATION" (se desbloquea tras explorar N veces), "CLUE" (se desbloquea al encontrar otra pista).
            - "unlockConditionValue": ID del sospechoso (para INTERROGATION), número de veces (para EXPLORATION), o ID de pista (para CLUE).
            
            Genera exactamente $suspectCount sospechosos y al menos 6 pistas.
            Asegurate de que las contradicciones sean sutiles y esten ligadas a las pistas.
            Solo devuelve el JSON, sin texto adicional.
        """.trimIndent()

        return try {
            val request = GroqRequest(
                messages = listOf(GroqMessage("user", prompt)),
                response_format = GroqResponseFormat()
            )
            val response = api.getCompletion(cleanApiKey, request)
            val content = response.choices.firstOrNull()?.message?.content ?: return null
            json.decodeFromString<Case>(content.trim())
        } catch (e: Exception) {
            Log.e("GroqService", "Error: ${e.message}")
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
            Tu coartada es: ${suspect.alibi}.
            Tu nivel de confianza actual es ${suspect.trustLevel}/100.
            Si tu confianza es baja (menor a 40), actua a la defensiva, nervioso o evasivo.
            Tus contradicciones conocidas son: ${suspect.contradictions.joinToString()}.
            Si mencionan una contradicción o algo relacionado, intenta negarlo nerviosamente.
            Responde de forma breve y natural en español. No uses asteriscos.
        """.trimIndent()
        
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

    suspend fun analyzeClueConnection(
        caseTitle: String,
        suspectName: String,
        clues: List<String>
    ): String {
        val prompt = """
            Como sistema operativo OBSIDIAN, confirma una deducción lógica.
            Caso: $caseTitle
            Sospechoso implicado: $suspectName
            Pistas conectadas: ${clues.joinToString(", ")}
            
            Escribe una conclusión técnica y breve (2 frases) en español que explique por qué estas pistas incriminan a este sospechoso. 
            Sé directo. No uses markdown.
        """.trimIndent()

        return try {
            val request = GroqRequest(messages = listOf(GroqMessage("user", prompt)), response_format = null)
            val response = api.getCompletion(cleanApiKey, request)
            response.choices.firstOrNull()?.message?.content ?: "Vínculo detectado: Los indicios apuntan a $suspectName."
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
            Solo devuelve el texto plano sin markdown.
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
