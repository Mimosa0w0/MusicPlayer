package com.example.musicplayer.recentplayModule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.MyApplication
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentRecentPlayBinding
import com.example.musicplayer.MyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecentPlayFragment : Fragment(R.layout.fragment_recent_play) {

    private var _binding: FragmentRecentPlayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by activityViewModels()
    private lateinit var adapter: RecentPlayAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecentPlayBinding.bind(view)
        adapter = RecentPlayAdapter(viewModel.musicRecentPlay, viewModel)
        binding.recentPlayList.run {
            layoutManager = LinearLayoutManager(MyApplication.context)
            adapter = this@RecentPlayFragment.adapter
        }

        if (viewModel.musicRecentPlay.isEmpty()) {
            viewModel.loadRecentPlay()
        }

        lifecycleScope.launch {
            viewModel.upDateRecentListFlow.collectLatest {
                adapter.notifyItemRangeChanged(0, it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}