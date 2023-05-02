package com.example.musicplayer.recentplayModule.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.recentplayModule.room.RecentPlayMusic

@Dao
interface RecentPlayDao {

    @Insert
    fun insertMusic(music: RecentPlayMusic)

    @Insert
    fun insertMusics(musics: List<RecentPlayMusic>)

    @Delete
    fun deleteMusic(music: RecentPlayMusic)

    @Query("SELECT * FROM recentplay WHERE title = :title")
    fun getMusicByTitle(title: String): RecentPlayMusic?

    @get:Query("SELECT * FROM recentplay")
    val allMusic: List<RecentPlayMusic>?

}