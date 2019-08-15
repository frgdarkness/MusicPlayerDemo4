package com.example.musicappdemo4.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicappdemo4.R

class SongAdapter(val listSong: ArrayList<Song>, listener: SongClick) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    val songClickListener: SongClick = listener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_row,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val s = listSong[position]
        holder.txtArtist.text = s.artist
        holder.txtTitle.text = s.title
        holder.imgCover.setImageResource(R.drawable.disc1)
        holder.itemView.setOnClickListener{
            songClickListener.onSongClick(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitleSongRow)
        val txtArtist = itemView.findViewById<TextView>(R.id.txtArtistSongRow)
        val imgCover = itemView.findViewById<ImageView>(R.id.imgCoverSongRow)
    }

    interface SongClick{
        //fun onSongClick(song:Song)
        fun onSongClick(index:Int)
    }
}
