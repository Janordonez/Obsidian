package com.example.obsidian.service

import com.example.obsidian.data.model.Clue
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppService {
    // Obtener todas las pistas
    @GET("clue/all")
    suspend fun getClues(): Response<List<Clue>>

    // Obtener pista/s por ID
    @GET("clue/getId/{id}")
    suspend fun getCluesById(@Path("id") id: String): Response<Clue>

    // Guardar una pista
    @POST("clue/save")
    suspend fun saveClue(@Body clue: Clue): Response<Clue>
}