package com.example.musicplayer.localModule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.MyApplication
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentLocalMusicBinding
import com.example.musicplayer.MyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocalMusicFragment : Fragment(R.layout.fragment_local_music) {

    private var _binding: FragmentLocalMusicBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by activityViewModels()
    private lateinit var adapter: LocalMusicListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocalMusicBinding.bind(view)
        adapter = LocalMusicListAdapter(viewModel.musicListLocal, viewModel)
        binding.localMusicList.run {
            layoutManager = LinearLayoutManager(MyApplication.context)
            adapter = this@LocalMusicFragment.adapter
        }

        lifecycleScope.launch {
            viewModel.loadLocalMusicFlow.collectLatest {
                adapter.notifyItemRangeChanged(0, it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}