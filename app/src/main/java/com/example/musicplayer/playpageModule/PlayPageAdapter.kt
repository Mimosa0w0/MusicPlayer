package com.example.musicplayer.playpageModule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.GlideApp
import com.example.musicplayer.MyApplication
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayPageBinding
import com.example.musicplayer.localModule.LocalMusic
import com.example.musicplayer.networkModule.Music
import com.example.musicplayer.MyViewModel

class PlayPageAdapter(
    private val networkMusicList: List<Music>,
    private val localMusicList: List<LocalMusic>,
    private val viewModel: MyViewModel,
    private val binding: FragmentPlayPageBinding
) : RecyclerView.Adapter<PlayPageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageFilterView = view.findViewById(R.id.pageCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cover, parent, false)
        val holder = ViewHolder(view)

        holder.itemView.setOnClickListener {
            binding.run {
                pager.visibility = View.INVISIBLE
                musicDetail.visibility = View.INVISIBLE
                lrcList.visibility = View.VISIBLE
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (viewModel.controllerBinder.type) {
            "NetworkMusic" -> {
                val music = networkMusicList[position]
                GlideApp.with(MyApplication.context).load(music.pic).into(holder.cover)
            }
            "LocalMusic" -> {
                val music = localMusicList[position]
                holder.cover.setImageBitmap(music.pic)
            }
        }
    }

    override fun getItemCount() = when (viewModel.controllerBinder.type) {
        "NetworkMusic" -> networkMusicList.size
        "LocalMusic" -> localMusicList.size
        else -> 0
    }

}