package com.example.obsidian.service

import com.example.obsidian.repository.AppRepository

object ServiceLocator {
    private val api = RetrofitClient.instance

    val appRepository = AppRepository(api)
}