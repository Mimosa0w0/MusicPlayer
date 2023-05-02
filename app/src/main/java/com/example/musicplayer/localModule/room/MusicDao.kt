package com.example.musicplayer.localModule.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MusicDao {

    @Insert
    fun insertMusic(music: Music)

    @Insert
    fun insertMusics(musics: List<Music>)

    @get:Query("SELECT * FROM musics")
    val allMusic: List<Music>?

    @Query("SELECT * FROM musics WHERE id = :id")
    fun getMusicById(id: Int): Music?

}