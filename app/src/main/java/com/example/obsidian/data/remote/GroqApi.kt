package com.example.obsidian.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: GroqRequest
    ): GroqResponse
}

@Serializable
data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    val temperature: Float = 0.5f,
    val response_format: GroqResponseFormat? = null
)

@Serializable
data class GroqResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>
)

@Serializable
data class GroqChoice(
    val message: GroqMessage
)