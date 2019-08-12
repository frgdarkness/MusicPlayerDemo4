package com.example.musicappdemo4.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.musicappdemo4.R
import com.example.musicappdemo4.data.model.Song
import com.example.musicappdemo4.service.MediaController
import com.example.musicappdemo4.service.MediaPlayerListener
import com.example.musicappdemo4.service.MusicService

class MediaPresenter() : MediaContract.Presenter {

    private var musicService: MusicService? = null
    private var mediaController: MediaPlayerListener? = null
    private var mainView: MediaContract.MainView? = null

    constructor(view:MediaContract.MainView) : this() {
        this.mainView = view
    }

    override fun setService(musicService: MusicService) {
        this.musicService = musicService
    }

    override fun onServiceConnected() {
        mediaController = musicService?.getMediaController()
    }

    override fun onServiceDisconnected() {
        mediaController = null
        musicService = null
    }

    override fun onPauseSong() {
        mediaController?.pauseSong()
    }

    override fun onPickSongToPlay(index: Int) {
        mediaController?.playSongAtIndex(index)
        Log.d("DEMO123","onPickSongToPlay: $index")
    }

    override fun registerReceiver(context: Context) {
        val filter = IntentFilter()
//        filter.addAction(MusicService.ACTION_NEXT)
//        filter.addAction(MusicService.ACTION_PLAY)
//        filter.addAction(MusicService.ACTION_PREV)
        filter.addAction(MusicService.ACTION_UPDATE_INFO_SONG)
        filter.addAction(MusicService.ACTION_UPDATE_STATUS_PLAY)
        context.registerReceiver(receiver,filter)
    }

    val receiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d(MusicService.TAG,"action: ${p1?.action}")
            when(p1?.action){
                MusicService.ACTION_UPDATE_INFO_SONG -> {
                    val song = p1.getSerializableExtra(MusicService.SONG_VALUE) as Song
                    mainView?.updateInfoSongNow(song)
                }

                MusicService.ACTION_UPDATE_STATUS_PLAY ->{
                    val isPlay = p1.getBooleanExtra(MusicService.PLAY_STATUS,false)
                    Log.d("DEMO123","isplay: $isPlay")
                    val icon = if(isPlay){ R.drawable.ic_pause_main} else{ R.drawable.ic_play_main }
                    mainView?.updateStatusPlay(icon)
                }
            }
        }

    }
}