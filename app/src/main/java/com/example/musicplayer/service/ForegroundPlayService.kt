package com.example.musicplayer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.musicplayer.R
import com.example.musicplayer.localModule.LocalMusic
import com.example.musicplayer.networkModule.Music
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

class ForegroundPlayService : Service() {

    companion object {
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
        const val PLAY = "play"
        const val PRE = "pre"
        const val NEXT = "next"
    }
    private val mBinder = ControllerBinder()
    private lateinit var remoteViews: RemoteViews
    private lateinit var notification: Notification
    private lateinit var musicReceiver: MusicReceiver

    override fun onCreate() {
        super.onCreate()
        mediaPlayer.run {
            setOnPreparedListener {
                runBlocking {
                    mBinder.upDatePlayStateFlow.emit("upDate")
                }
                mBinder.start()
            }
        }
        val manager = getSystemService<NotificationManager>()
        val channel = NotificationChannel("musicplay_service", "musicplayerchannel",
            NotificationManager.IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)
        initRemoteViews()
        notification = NotificationCompat.Builder(this, "musicplay_service")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContent(remoteViews)
            .build()
        registerBroadcast()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?) = mBinder

    inner class ControllerBinder : Binder() {
        var index: Int? = null
        var type: String? = null
        val musicListNetwork = ArrayList<Music>()
        val musicListLocal = ArrayList<LocalMusic>()
        val upDateCurrentPlayFlow = MutableStateFlow(mapOf(index to type))
        val upDatePlayStateFlow = MutableSharedFlow<String>(0)

        fun startMusicNetwork(index: Int) {
            this.index = index
            val music = musicListNetwork[index]
            remoteViews.run {
                setTextViewText(R.id.foregroundMusicName, music.title)
                setTextViewText(R.id.foregroundArtist, music.author)
            }
            startMusic(music.url)
        }

        fun startMusicLocal(index: Int) {
            this.index = index
            val music = musicListLocal[index]
            remoteViews.run {
                setTextViewText(R.id.foregroundMusicName, music.title)
                setTextViewText(R.id.foregroundArtist, music.author)
            }
            startMusic(music.data!!)
        }

        private fun startMusic(data: String) {
            val time = Timer()
            time.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        mediaPlayer.run {
                            reset()
                            setDataSource(data)
                            prepareAsync()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        time.cancel()
                    }
                }
            }, 0, 500)
        }

        fun pause() {
            remoteViews.setImageViewResource(R.id.foregroundPlay, R.drawable.ic_baseline_play_circle_filled_24)
            mediaPlayer.pause()
            runBlocking {
                mBinder.upDatePlayStateFlow.emit("Pause")
            }
            startForeground(1, notification)
        }

        fun start() {
            remoteViews.setImageViewResource(R.id.foregroundPlay, R.drawable.ic_baseline_pause_circle_filled_24)
            mediaPlayer.start()
            runBlocking {
                mBinder.upDatePlayStateFlow.emit("Play")
            }
            startForeground(1, notification)
        }
    }

    private inner class MusicReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                PRE -> {
                    when (mBinder.type) {
                        "NetworkMusic" -> {
                            if (mBinder.index == 0) {
                                mBinder.startMusicNetwork(mBinder.musicListNetwork.size - 1)
                            } else {
                                mBinder.startMusicNetwork(mBinder.index!! - 1)
                            }
                        }
                        "LocalMusic" -> {
                            if (mBinder.index == 0) {
                                mBinder.startMusicLocal(mBinder.musicListLocal.size - 1)
                            } else {
                                mBinder.startMusicLocal(mBinder.index!! - 1)
                            }
                        }
                    }
                    runBlocking {
                        mBinder.upDateCurrentPlayFlow.emit(mapOf(mBinder.index to mBinder.type))
                    }
                }
                PLAY -> {
                    if (mediaPlayer.isPlaying) {
                        mBinder.pause()
                    } else {
                        mBinder.start()
                    }
                }
                NEXT -> {
                    when (mBinder.type) {
                        "NetworkMusic" -> {
                            if (mBinder.index == mBinder.musicListNetwork.size - 1) {
                                mBinder.startMusicNetwork(0)
                            } else {
                                mBinder.startMusicNetwork(mBinder.index!! + 1)
                            }
                        }
                        "LocalMusic" -> {
                            if (mBinder.index == mBinder.musicListLocal.size - 1) {
                                mBinder.startMusicLocal(0)
                            } else {
                                mBinder.startMusicLocal(mBinder.index!! + 1)
                            }
                        }
                    }
                    runBlocking {
                        mBinder.upDateCurrentPlayFlow.emit(mapOf(mBinder.index to mBinder.type))
                    }
                }
            }
        }
    }

    private fun initRemoteViews() {
        remoteViews = RemoteViews(packageName, R.layout.foreground_play_layout).apply {
            val intentPre = Intent(PRE)
            val pendingIntentPre = PendingIntent.getBroadcast(this@ForegroundPlayService, 0, intentPre, 0)
            setOnClickPendingIntent(R.id.foregroundPre, pendingIntentPre)
            val intentPlay = Intent(PLAY)
            val pendingIntentPlay = PendingIntent.getBroadcast(this@ForegroundPlayService, 0, intentPlay, 0)
            setOnClickPendingIntent(R.id.foregroundPlay, pendingIntentPlay)
            val intentNext= Intent(NEXT)
            val pendingIntentNext = PendingIntent.getBroadcast(this@ForegroundPlayService, 0, intentNext, 0)
            setOnClickPendingIntent(R.id.foregroundNext, pendingIntentNext)
        }
    }

    private fun registerBroadcast() {
        musicReceiver = MusicReceiver()
        val intentFilter = IntentFilter().apply {
            addAction(PLAY)
            addAction(PRE)
            addAction(NEXT)
        }
        registerReceiver(musicReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        unregisterReceiver(musicReceiver)
    }

}


