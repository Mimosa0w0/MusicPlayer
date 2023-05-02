package com.example.musicplayer.networkModule

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.MyApplication
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentNetworkMusicBinding
import com.example.musicplayer.MyViewModel
import kotlinx.coroutines.flow.collectLatest

class NetworkMusicFragment : Fragment(R.layout.fragment_network_music) {

    private var _binding: FragmentNetworkMusicBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by activityViewModels()
    private lateinit var adapter: NetworkMusicListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNetworkMusicBinding.bind(view)
        adapter = NetworkMusicListAdapter(viewModel.musicListNetwork, viewModel)
        binding.musicList.run {
            layoutManager = LinearLayoutManager(MyApplication.context)
            adapter = this@NetworkMusicFragment.adapter
        }

        //搜索音乐并更新到列表上
        binding.search.setOnClickListener {
            val inputMethodManager = requireActivity().getSystemService<InputMethodManager>()
            inputMethodManager?.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            binding.musicList.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launchWhenResumed {
                viewModel.searchMusic(binding.searchEdit.text.toString()).collectLatest {
                    val data = it.getOrNull()
                    viewModel.musicListNetwork.clear()
                    if (data != null) {
                        viewModel.musicListNetwork.addAll(data)
                    } else {
                        Toast.makeText(MyApplication.context, "未查询到", Toast.LENGTH_SHORT).show()
                        it.exceptionOrNull()?.printStackTrace()
                    }
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
                binding.progressBar.visibility = View.INVISIBLE
                binding.musicList.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}