package com.example.musicplayer.networkModule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.GlideApp
import com.example.musicplayer.MyApplication
import com.example.musicplayer.R
import com.example.musicplayer.MyViewModel

class NetworkMusicListAdapter(
    private val musicList: List<Music>,
    private val viewModel: MyViewModel
) : RecyclerView.Adapter<NetworkMusicListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageFilterView = view.findViewById(R.id.cover)
        val musicName: TextView = view.findViewById(R.id.musicName)
        val artist: TextView = view.findViewById(R.id.artist)
        val index: TextView = view.findViewById(R.id.index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        val holder = ViewHolder(view)

        holder.itemView.setOnClickListener {
            val index = holder.index.text.toString().toInt()
            if (viewModel.controllerBinder.musicListNetwork != viewModel.musicListNetwork) {
                viewModel.controllerBinder.musicListNetwork.clear()
                viewModel.controllerBinder.musicListNetwork.addAll(viewModel.musicListNetwork)
            }
            viewModel.run {
                controllerBinder.startMusicNetwork(index)
                upDateCurrentPlay(index, "NetworkMusic")
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        GlideApp.with(MyApplication.context).load(music.pic).into(holder.cover)
        holder.run {
            musicName.text = music.title
            artist.text = music.author
            index.text = position.toString()
        }
    }

    override fun getItemCount() = musicList.size

}