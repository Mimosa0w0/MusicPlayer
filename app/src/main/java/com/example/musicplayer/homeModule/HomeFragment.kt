package com.example.musicplayer.homeModule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.run {
            search.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_networkMusicFragment)
            }
            localMusic.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_localMusicFragment)
            }
            recentPlay.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_recentPlayFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}