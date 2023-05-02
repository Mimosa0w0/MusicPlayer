package com.example.musicplayer.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayer.MyApplication
import com.example.musicplayer.localModule.room.Music
import com.example.musicplayer.localModule.room.MusicDao
import com.example.musicplayer.recentplayModule.room.RecentPlayDao
import com.example.musicplayer.recentplayModule.room.RecentPlayMusic

@Database(entities = [Music::class, RecentPlayMusic::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun MusicDao(): MusicDao
    abstract fun RecentPlayDao(): RecentPlayDao

    companion object {
        @Volatile
        private var sInstance: MusicDatabase? = null
        private const val DATA_BASE_NAME = "musics.db"

        @JvmStatic
        fun getInstance(): MusicDatabase? {
            if (sInstance == null) {
                synchronized(MusicDatabase::class.java) {
                    if (sInstance == null) {
                        sInstance = createInstance()
                    }
                }
            }
            return sInstance
        }

        private fun createInstance() =
            Room.databaseBuilder(
                MyApplication.context,
                MusicDatabase::class.java,
                DATA_BASE_NAME).build()
    }

}