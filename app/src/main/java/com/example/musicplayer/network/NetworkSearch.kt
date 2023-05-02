package com.example.musicplayer.network

import android.widget.Toast
import com.example.musicplayer.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object NetworkSearch {

    private val musicSearchService = ServiceCreator.create<MusicSearchService>()

    suspend fun searchMusic(text: String) = musicSearchService.searchMusic(text).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine {
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) it.resume(body)
                    else Toast.makeText(MyApplication.context, "body is null", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    Toast.makeText(MyApplication.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}