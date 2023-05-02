package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.example.musicplayer.database.MusicDatabase

class MyApplication: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var musicDb: MusicDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        musicDb = MusicDatabase.getInstance()
    }

}