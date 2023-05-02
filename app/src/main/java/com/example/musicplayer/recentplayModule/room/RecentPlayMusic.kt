package com.example.musicplayer.recentplayModule.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recentplay")
class RecentPlayMusic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val title: String,
    @ColumnInfo val author: String,
    @ColumnInfo val pic: ByteArray?,
    @ColumnInfo val index: Int
)