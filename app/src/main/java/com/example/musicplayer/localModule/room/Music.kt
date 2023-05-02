package com.example.musicplayer.localModule.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "musics")
class Music(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val title: String,
    @ColumnInfo val author: String,
    @ColumnInfo val data: String,
    @ColumnInfo val pic: ByteArray?
)