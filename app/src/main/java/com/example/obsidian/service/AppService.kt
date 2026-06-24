package com.example.obsidian.service

import com.example.obsidian.data.model.Case
import com.example.obsidian.data.model.Clue
import com.example.obsidian.data.model.Question
import com.example.obsidian.data.model.Suspect
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AppService {
    // Obtener todos los datos
    @GET("case/all")
    suspend fun getCases(): Response<List<Case>>
    @GET("suspect/all")
    suspend fun getSuspects(): Response<List<Suspect>>
    @GET("question/all")
    suspend fun getQuestion(): Response<List<Question>>
    @GET("clue/all")
    suspend fun getClues(): Response<List<Clue>>

    // Obtener datos por medio de ID
    @GET("case/getid/{id}")
    suspend fun getCasesById(@Path("id") id: String): Response<Case>
    @GET("suspect/getId/{id}")
    suspend fun getSuspectById(@Path("id") id: String): Response<Suspect>
    @GET("question/getId/{id}")
    suspend fun getQuestionById(@Path("id") id: String): Response<Question>
    @GET("clue/getId/{id}")
    suspend fun getCluesById(@Path("id") id: String): Response<Clue>

    // Guardar un dato de la investigacion
    @POST("case/save")
    suspend fun saveCase(@Body case: Case): Response<Case>
    @POST("suspect/save")
    suspend fun saveSuspect(@Body suspect: Suspect): Response<Suspect>
    @POST("question/save")
    suspend fun saveQuestion(@Body question: Question): Response<Question>
    @POST("clue/save")
    suspend fun saveClue(@Body clue: Clue): Response<Clue>

    // Actulixar un dato de la investigación
    @PUT("case/update/{id}")
    suspend fun updateCase(@Path("id") id: String, @Body case: Case): Response<Case>
    @PUT("suspect/update/{id}")
    suspend fun updateSuspect(@Path("id") id: String, @Body suspect: Suspect): Response<Suspect>
    @PUT("question/update/{id}")
    suspend fun updateQuestion(@Path("id") id: String, @Body question: Question): Response<Question>
    @PUT("clue/update/{id}")
    suspend fun updateClue(@Path("id") id: String, @Body clue: Clue): Response<Clue>

    // Eliminar un dato por medio del ID
    @DELETE("case/delete/{id}")
    suspend fun deleteCase(@Path("id") id: String): Response<Unit>
    @DELETE("suspect/delete/{id}")
    suspend fun deleteSuspect(@Path("id") id: String): Response<Unit>
    @DELETE("question/delete/{id}")
    suspend fun deleteQuestion(@Path("id") id: String): Response<Unit>
    @DELETE("clue/delete/{id}")
    suspend fun deleteClue(@Path("id") id: String): Response<Unit>
}