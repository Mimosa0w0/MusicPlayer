package com.example.musicplayer.playpageModule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayPageBinding
import com.google.android.material.textview.MaterialTextView

class LrcListAdapter(
    private val lrcList: List<String>,
    private val binding: FragmentPlayPageBinding
    ) : RecyclerView.Adapter<LrcListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lrc: MaterialTextView = view.findViewById(R.id.lrc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lrc, parent, false)
        val holder = ViewHolder(view)

        holder.itemView.setOnClickListener {
            binding.run {
                lrcList.visibility = View.INVISIBLE
                pager.visibility = View.VISIBLE
                musicDetail.visibility = View.VISIBLE
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lrc = lrcList[position]
        holder.lrc.text = lrc
    }

    override fun getItemCount() = lrcList.size
}