package com.example.musicplayer.recentplayModule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.localModule.LocalMusic
import com.example.musicplayer.MyViewModel

class RecentPlayAdapter(
    private val recentPlayList: List<LocalMusic>,
    private val viewModel: MyViewModel
) : RecyclerView.Adapter<RecentPlayAdapter.ViewHolder>() {

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
                val id = recentPlayList[index].index
                controllerBinder.startMusicLocal(id)
                upDateCurrentPlay(id, "LocalMusic")
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = recentPlayList[position]
        holder.run {
            musicName.text = music.title
            artist.text = music.author
            index.text = position.toString()
            cover.setImageBitmap(music.pic)
        }
    }

    override fun getItemCount() = recentPlayList.size

}