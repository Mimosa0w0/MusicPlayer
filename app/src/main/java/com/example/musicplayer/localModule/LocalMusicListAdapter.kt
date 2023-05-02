package com.example.musicplayer.localModule

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.MyViewModel

class LocalMusicListAdapter(
    private val musicList: List<LocalMusic>,
    private val viewModel: MyViewModel,
    ) : RecyclerView.Adapter<LocalMusicListAdapter.ViewHolder>() {

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
            if (viewModel.controllerBinder.musicListLocal.isEmpty()) {
                viewModel.controllerBinder.musicListLocal.addAll(viewModel.musicListLocal)
            }
            val index = holder.index.text.toString().toInt()
            viewModel.run {
                Log.d("Mimosa", "Adapter Play")
                controllerBinder.startMusicLocal(index)
                upDateCurrentPlay(index, "LocalMusic")
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        holder.run {
            musicName.text = music.title
            artist.text = music.author
            index.text = position.toString()
            cover.setImageBitmap(music.pic)
        }
    }

    override fun getItemCount() = musicList.size
}