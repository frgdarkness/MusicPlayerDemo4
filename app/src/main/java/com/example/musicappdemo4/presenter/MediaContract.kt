package com.example.musicappdemo4.presenter

import android.content.Context
import com.example.musicappdemo4.data.model.Song
import com.example.musicappdemo4.service.MusicService

interface MediaContract {
    interface MainView{
        fun updateInfoSongNow(song: Song)
        //fun updateStatusPlay(isPlay: Boolean)
        fun updateStatusPlay(icon:Int)
    }

    interface Presenter{

        fun setService(musicService: MusicService)

        fun onServiceConnected()

        fun onServiceDisconnected()

        fun registerReceiver(context: Context)

        fun onPauseSong()

        fun onPickSongToPlay(index:Int)

    }
}