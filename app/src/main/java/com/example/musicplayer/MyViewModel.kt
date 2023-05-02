package com.example.musicplayer

import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.MyApplication.Companion.musicDb
import com.example.musicplayer.localModule.LocalMusic
import com.example.musicplayer.networkModule.Music
import com.example.musicplayer.recentplayModule.room.RecentPlayMusic
import com.example.musicplayer.service.ForegroundPlayService
import com.example.musicplayer.service.ForegroundPlayService.Companion.mediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    private val regex = Regex("(\\[\\d{2}:\\d{2}\\.\\d+])+")
    //页面所需数据
    val musicListNetwork = ArrayList<Music>()
    val musicListLocal = ArrayList<LocalMusic>()
    val musicRecentPlay = ArrayList<LocalMusic>()
    val lrcList = ArrayList<String>()
    var stopProgress = false
    lateinit var controllerBinder: ForegroundPlayService.ControllerBinder
    private val _upDateRecentListFlow = MutableSharedFlow<Int>(0)
    val upDateRecentListFlow: SharedFlow<Int> get() = _upDateRecentListFlow
    private val _loadLocalMusicFlow = MutableSharedFlow<Int>(0)
    val loadLocalMusicFlow: SharedFlow<Int> get() = _loadLocalMusicFlow

    fun loadLocalMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            musicDb?.MusicDao()?.allMusic?.forEach {
                musicListLocal.add(LocalMusic(it.id - 1, it.title, it.author, it.data, getCover(it.pic)))
            }
            _loadLocalMusicFlow.emit(musicListLocal.size)
        }
    }

    fun loadRecentPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            musicDb?.RecentPlayDao()?.allMusic?.forEach {
                musicRecentPlay.add(LocalMusic(it.index, it.title, it.author, pic = getCover(it.pic)))
            }
            _upDateRecentListFlow.emit(musicRecentPlay.size)
        }
    }

    //搜索音乐
    fun searchMusic(title: String) = flow {
        if (title.isEmpty()) {
            Toast.makeText(MyApplication.context, "输入为空", Toast.LENGTH_SHORT).show()
            return@flow
        }
        emit(Repository.searchMusic(title))
    }

    //读取本地音乐
    fun readMusic(cursor: Cursor?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        val author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                        val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val pic = getCoverData(data)
                        musicDb?.MusicDao()?.insertMusic(
                            com.example.musicplayer.localModule.room.Music(
                                title = title, author = author, data = data, pic = pic)
                        )
                    }
                    loadLocalMusic()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun upDateRecentList(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val musicDao = musicDb?.MusicDao()
            val recentPlayDao = musicDb?.RecentPlayDao()
            val music = musicDao?.getMusicById(index + 1)!!
            if (recentPlayDao?.getMusicByTitle(music.title) == null) {
                recentPlayDao!!.insertMusic(RecentPlayMusic(title = music.title, author = music.author, pic = music.pic, index = index))
            } else {
                recentPlayDao.deleteMusic(recentPlayDao.getMusicByTitle(music.title)!!)
                recentPlayDao.insertMusic(RecentPlayMusic(title = music.title, author = music.author, pic = music.pic, index = index))
            }
            musicRecentPlay.clear()
            loadRecentPlay()
        }
    }

    //更新当前播放
    fun upDateCurrentPlay(index: Int, type: String) {
        viewModelScope.launch {
            controllerBinder.upDateCurrentPlayFlow.emit(mapOf(index to type))
        }
    }

    //设置歌词
    fun setLyric(lrc: String) {
        lrcList.clear()
        if (lrc == "") {
            lrcList.add("暂无歌词")
            return
        }
        lrcList.addAll(regex.split(lrc).filter {
            it != "\n"
        })
    }

    private fun getCoverData(data: String): ByteArray? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(data)
        return mediaMetadataRetriever.embeddedPicture
    }

    private fun getCover(data: ByteArray?) = data?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    //更新进度条
    fun upDateProgress() = flow {
        while (mediaPlayer.currentPosition <= mediaPlayer.duration) {
            if (!stopProgress) {
                emit(mediaPlayer.currentPosition)
            }
            delay(1000)
        }
    }

}