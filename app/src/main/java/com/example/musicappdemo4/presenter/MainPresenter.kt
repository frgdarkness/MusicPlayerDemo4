package com.example.musicappdemo4.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.musicappdemo4.R
import com.example.musicappdemo4.model.App
import com.example.musicappdemo4.model.Song
import com.example.musicappdemo4.service.MediaPlayerListener
import com.example.musicappdemo4.service.MusicService

class MainPresenter() : MainContract.Presenter {

    private var musicService: MusicService? = null
    private var mediaController: MediaPlayerListener? = null
    private var mainView: MainContract.MainView? = null
    private var playView: MainContract.PlayView? = null
    private val handler = android.os.Handler()
    private var runnable:Runnable? = null

    constructor(view:MainContract.MainView) : this() {
        this.mainView = view
    }

    constructor(view:MainContract.PlayView) : this(){
        this.playView = view
    }

    override fun setService(musicService: MusicService) {
        this.musicService = musicService
    }

    override fun onServiceConnected() {
        mediaController = musicService?.getMediaController()
        if(mediaController?.isPlaying()!!){
            val song = mediaController?.getSongCurrent()
            if (song != null) {
                mainView?.updateInfoSongNow(song)
                playView?.updateInfoSongNowActiPlay(song)
            }
        }

    }

    override fun onServiceDisconnected() {
        mediaController = null
        musicService = null
    }

    override fun onPauseSong() {
        mediaController?.pauseSong()
    }

    override fun onNextSong() {
        mediaController?.nextSong()
        setTimeSong()
    }

    override fun onPrevSong() {
        mediaController?.prevSong()
        setTimeSong()
    }

    override fun onSeekTo(progress: Int) {
        mediaController?.seekTo(progress)
    }

    override fun onPickSongToPlay(index: Int) {
        mediaController?.playSongAtIndex(index)
        Log.d(App.TAG,"onPickSongToPlay: $index")
    }

    fun updateTimeSong(){
        val temp = mediaController?.getCurrentTime()
        if(temp!=null) playView?.updateSeekbar(temp)
        //Log.d(App.TAG,"timenow: $temp")
        runnable = Runnable {
            updateTimeSong()
        }
        handler.postDelayed(runnable!!,1000)
    }


    override fun setTimeSong(){
        runnable?.let {
            handler.removeCallbacks(it)
        }
        updateTimeSong()
    }

    override fun stop() {
        runnable?.let{
            handler.removeCallbacks(it)
        }
        mediaController = null
        musicService = null
    }

    override fun exitActiPlay() {
        runnable?.let{
            handler.removeCallbacks(it)
        }
    }

    override fun registerReceiver(context: Context) {
        val filter = IntentFilter()
        filter.addAction(App.ACTION_UPDATE_INFO_SONG)
        filter.addAction(App.ACTION_UPDATE_STATUS_PLAY)
        filter.addAction(App.ACTION_EXIT)
        context.registerReceiver(receiver,filter)
    }

    override fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }

    val receiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d(App.TAG,"action: ${p1?.action}")
            when(p1?.action){
                App.ACTION_UPDATE_INFO_SONG -> {
                    val song = p1.getSerializableExtra(App.SONG_VALUE) as Song
                    mainView?.updateInfoSongNow(song)
                    playView?.updateInfoSongNowActiPlay(song)
                }

                App.ACTION_UPDATE_STATUS_PLAY -> {
                    val isPlay = p1.getBooleanExtra(App.PLAY_STATUS,false)
                    Log.d(App.TAG,"isplay: $isPlay")
                    val iconMain = if(isPlay){ R.drawable.ic_pause_main} else{ R.drawable.ic_play_main }
                    val iconPlay = if(isPlay){ R.drawable.ic_pause_2} else{ R.drawable.ic_play_2 }
                    mainView?.updateStatusPlay(iconMain)
                    playView?.updateStatusPLayActiPlay(iconPlay)
                }

                App.ACTION_EXIT -> {
                    exitActiPlay()
                    playView?.exitActivityPlay()
                    mainView?.exitMain()
                }
            }
        }

    }
}
