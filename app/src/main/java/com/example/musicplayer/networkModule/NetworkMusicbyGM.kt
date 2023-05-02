package com.example.musicplayer.networkModule

data class NetworkMusicbyGM(val code: Int, val msg: String, val data: List<Music>)
data class Music(
    val title: String,
    val author: String,
    val lrc: String = "",
    val url: String,
    val pic: String
)