package com.example.obsidian.repository

import com.example.obsidian.data.model.Clue
import com.example.obsidian.service.ApiResult
import com.example.obsidian.service.AppService

class AppRepository (private val api : AppService) {
    suspend fun findAll(): ApiResult<List<Clue>> {
        return try {
            val response = api.getClues()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            }
            else {
                ApiResult.Error("Error HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun findById(id: String) : ApiResult<Clue>
    {
        return try {
            val response = api.getCluesById(id)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            }
            else {
                ApiResult.Error("Error HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun save(clue: Clue) : ApiResult<Clue>
    {
        return try {
            val response = api.saveClue(clue)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Error HTTP ${response.code()}")
            }
        }
        catch(e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}
