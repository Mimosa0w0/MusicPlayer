package com.example.musicplayer.network

import com.example.musicplayer.networkModule.NetworkMusicbyGM
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicSearchService {

    @GET("Music?format=json&site=netease")
    fun searchMusic(@Query("text") text: String, @Query("page") page: String = "1"): Call<NetworkMusicbyGM>

}