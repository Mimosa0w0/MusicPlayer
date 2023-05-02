package com.example.musicplayer.playpageModule

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayPageBinding
import com.example.musicplayer.playpageModule.transformer.ScaleInTransformer
import com.example.musicplayer.service.ForegroundPlayService.Companion.mediaPlayer
import com.example.musicplayer.MyViewModel
import kotlinx.coroutines.flow.collectLatest


class PlayPageFragment : Fragment(R.layout.fragment_play_page) {

    private val flag = "PlayPageFragment"
    private var _binding: FragmentPlayPageBinding? = null
    val binding get() = _binding!!
    private val viewModel: MyViewModel by activityViewModels()
    private lateinit var pageAdapter: PlayPageAdapter
    private lateinit var lrcAdapter: LrcListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayPageBinding.bind(view)
        pageAdapter = PlayPageAdapter(viewModel.controllerBinder.musicListNetwork,
            viewModel.controllerBinder.musicListLocal,
            viewModel,
            binding)
        lrcAdapter = LrcListAdapter(viewModel.lrcList, binding)

        if (mediaPlayer.isPlaying) {
            binding.pagePlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
        }

        binding.lrcList.run {
            adapter = lrcAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            (getChildAt(0) as RecyclerView).run {
                overScrollMode = View.OVER_SCROLL_NEVER
                val padding = resources.getDimensionPixelOffset(R.dimen.dp_95) +
                        resources.getDimensionPixelOffset(R.dimen.dp_95)
                setPadding(0, padding, 0, padding)
                clipToPadding = false
            }
        }

        //音乐封面配置与处理封面切换
        binding.pager.run {
            adapter = pageAdapter
            offscreenPageLimit = 3
            layoutParams.width += 850
            setPageTransformer(ScaleInTransformer())
            (getChildAt(0) as RecyclerView).overScrollMode = View.OVER_SCROLL_NEVER
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (viewModel.controllerBinder.type) {
                        "NetworkMusic" -> {
                            val music = viewModel.controllerBinder.musicListNetwork[position]
                            binding.run {
                                pageMusicName.text = music.title
                                pageArtist.text = music.author
                                viewModel.setLyric(music.lrc)
                                lrcAdapter.notifyDataSetChanged()
                                lrcList.setCurrentItem(0, false)
                            }
                        }
                        "LocalMusic" -> {
                            val music = viewModel.controllerBinder.musicListLocal[position]
                            binding.run {
                                pageMusicName.text = music.title
                                pageArtist.text = music.author
                                viewModel.setLyric("")
                                lrcAdapter.notifyDataSetChanged()
                                lrcList.setCurrentItem(0, false)
                            }
                        }
                    }
                    if (position != viewModel.controllerBinder.index) {
                        viewModel.stopProgress = true
                        binding.run {
                            pagePlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                            seekBar.progress = 0
                            currentTime.text = String.format("%02d:%02d", 0, 0)
                        }
                        viewModel.upDateCurrentPlay(currentItem, viewModel.controllerBinder.type!!)
                    }
                }
            })
        }
        pageAdapter.notifyItemRangeChanged(0, pageAdapter.itemCount)

        binding.run {
            viewModel.controllerBinder.index?.let { pager.setCurrentItem(it, false) }
            seekBar.max = mediaPlayer.duration
            val time = mediaPlayer.duration / 1000
            duration.text = String.format("%02d:%02d", time / 60, time % 60)
        }

        //时间自动更新
        lifecycleScope.launchWhenResumed {
            viewModel.upDateProgress().collectLatest {
                val time = it / 1000
                binding.run {
                    seekBar.progress = it
                    currentTime.text = String.format("%02d:%02d", time / 60, time % 60)
                }
            }
        }
        
        //音乐暂停/开始、上一首、下一首处理逻辑
        binding.run {
            pagePre.setOnClickListener {
                if (viewModel.controllerBinder.index == 0) {
                    pager.setCurrentItem(pageAdapter.itemCount - 1, false)
                } else {
                    pager.currentItem = viewModel.controllerBinder.index!! - 1
                }
            }

            pagePlay.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    viewModel.controllerBinder.pause()
                } else {
                    viewModel.controllerBinder.start()
                }
            }

            pageNext.setOnClickListener {
                if (viewModel.controllerBinder.index == pageAdapter.itemCount - 1) {
                    pager.setCurrentItem(0, false)
                } else {
                    pager.currentItem = viewModel.controllerBinder.index!! + 1
                }
            }
        }

        //音乐进度条拖动逻辑
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progress = 0

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                progress = p1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                viewModel.stopProgress = true
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.stopProgress = false
                binding.pagePlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                mediaPlayer.seekTo(progress)
                mediaPlayer.start()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}