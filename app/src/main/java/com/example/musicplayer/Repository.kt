package com.example.musicplayer

import com.example.musicplayer.network.NetworkSearch
import com.example.musicplayer.networkModule.Music

object Repository {

    suspend fun searchMusic(text: String): Result<List<Music>> {
        val response = NetworkSearch.searchMusic(text)
        val result = try {
            if (response.code == 200) {
                Result.success(response.data)
            } else {
                Result.failure(RuntimeException(response.msg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

        return result
    }

}