package com.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.homeModule.HomeFragment
import com.example.musicplayer.localModule.LocalMusicFragment
import com.example.musicplayer.networkModule.NetworkMusicFragment
import com.example.musicplayer.playpageModule.PlayPageFragment
import com.example.musicplayer.recentplayModule.RecentPlayFragment
import com.example.musicplayer.service.ForegroundPlayService
import com.example.musicplayer.service.ForegroundPlayService.Companion.mediaPlayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MyViewModel by viewModels()
    private var cursor: Cursor? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            viewModel.controllerBinder = p1 as ForegroundPlayService.ControllerBinder
            upDateCurrentPlay()
            upDatePlayState()
        }
        override fun onServiceDisconnected(p0: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.run {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        runtimePermission()
        val intent = Intent(this, ForegroundPlayService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        binding.play.setOnClickListener {
            if (viewModel.controllerBinder.index == null) return@setOnClickListener
            if (mediaPlayer.isPlaying) {
                viewModel.controllerBinder.pause()
            } else {
                viewModel.controllerBinder.start()
            }
        }

        //进入播放页
        binding.playDescription.setOnClickListener {
            if (viewModel.controllerBinder.index == null) {
                Toast.makeText(MyApplication.context, "当前无音乐", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val des = when(getCurrentFragment()) {
                is HomeFragment -> R.id.action_homeFragment_to_playPageFragment
                is LocalMusicFragment -> R.id.action_localMusicFragment_to_playPageFragment
                is NetworkMusicFragment -> R.id.action_networkMusicFragment_to_playPageFragment
                is RecentPlayFragment -> R.id.action_recentPlayFragment_to_playPageFragment
                else -> R.id.playPageFragment
            }
            binding.playControls.visibility = View.INVISIBLE
            findNavController(R.id.fragmentContainerView).navigate(des)
        }
    }

    private fun upDatePlayState() {
        lifecycleScope.launch {
            viewModel.controllerBinder.upDatePlayStateFlow.collectLatest { state ->
                when (val fragment = getCurrentFragment()) {
                    is PlayPageFragment -> {
                        when (state) {
                            "upDate" -> {
                                Log.d("Mimosa", "upDate")
                                fragment.binding.run {
                                    val time = mediaPlayer.duration / 1000
                                    duration.text = String.format("%02d:%02d", time / 60, time % 60)
                                    seekBar.max = mediaPlayer.duration
                                }
                                viewModel.stopProgress = false
                            }
                            "Pause" -> {
                                Log.d("Mimosa", "Pause")
                                binding.play.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                                fragment.binding.pagePlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                            }
                            "Play" -> {
                                Log.d("Mimosa", "Play")
                                binding.play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                                fragment.binding.pagePlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                            }
                        }
                    }
                    else -> {
                        when (state) {
                            "Pause" -> binding.play.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                            "Play" -> binding.play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                        }
                    }
                }
            }
        }
    }

    private fun upDateCurrentPlay() {
        lifecycleScope.launchWhenResumed {
            viewModel.controllerBinder.run {
                upDateCurrentPlayFlow.collectLatest {
                    val index_ = it.entries.map { entry -> entry.key }[0]
                    binding.play.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                    if (mediaPlayer.isPlaying) {
                        binding.play.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                    }
                    index_?.let { index ->
                        type = it[index]
                        when (type) {
                            "NetworkMusic" -> {
                                val music = musicListNetwork[index]
                                binding.run {
                                    playMusicName.text = music.title
                                    playArtist.text = music.author
                                    playCover.visibility = View.INVISIBLE
                                    GlideApp.with(this@MainActivity).load(music.pic).into(playCover)
                                    playCover.visibility = View.VISIBLE
                                }
                                when (val fragment = getCurrentFragment()) {
                                    is PlayPageFragment -> {
                                        fragment.binding.pager.currentItem = index
                                        if (index != viewModel.controllerBinder.index) {
                                            startMusicNetwork(index)
                                        }
                                    }
                                }
                            }
                            "LocalMusic" -> {
                                val music = musicListLocal[index]
                                binding.run {
                                    playMusicName.text = music.title
                                    playArtist.text = music.author
                                    playCover.visibility = View.INVISIBLE
                                    playCover.setImageBitmap(music.pic)
                                    playCover.visibility = View.VISIBLE
                                }
                                when (val fragment = getCurrentFragment()) {
                                    is PlayPageFragment -> {
                                        fragment.binding.pager.currentItem = index
                                        if (index != viewModel.controllerBinder.index) {
                                            startMusicLocal(index)
                                        }
                                    }
                                }
                                viewModel.upDateRecentList(index)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.playControls.visibility == View.INVISIBLE) {
           binding.playControls.visibility = View.VISIBLE
        }
    }

    private fun runtimePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            viewModel.loadLocalMusic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null)
                viewModel.readMusic(cursor)
            } else {
                Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentFragment(): Fragment {
        val navHostFragment = supportFragmentManager.fragments.first() as NavHostFragment
        return navHostFragment.childFragmentManager.fragments.first()
    }

}