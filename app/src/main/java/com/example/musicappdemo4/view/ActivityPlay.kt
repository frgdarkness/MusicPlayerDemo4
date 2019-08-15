package com.example.musicappdemo4.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.example.musicappdemo4.R
import com.example.musicappdemo4.model.App
import com.example.musicappdemo4.model.MyMedia
import com.example.musicappdemo4.model.Song
import com.example.musicappdemo4.presenter.MainContract
import com.example.musicappdemo4.presenter.MainPresenter
import com.example.musicappdemo4.service.MusicService
import kotlinx.android.synthetic.main.activity_play.*
import java.text.SimpleDateFormat

class ActivityPlay : AppCompatActivity(), MainContract.PlayView, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    var intentService: Intent? = null
    var musicService: MusicService? = null
    var connection: ServiceConnection? = null
    var presenter: MainContract.Presenter? = null
    var isBound = false
    var progessSeekBar = 0
    var timeFormat = SimpleDateFormat("mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        presenter = MainPresenter(this)
        presenter?.registerReceiver(this)
        bindService()
        btnPlayActiPlay.setOnClickListener(this)
        btnNextActiPlay.setOnClickListener(this)
        btnPrevActiPlay.setOnClickListener(this)
        sbSongActiPlay.setOnSeekBarChangeListener(this)
    }

    fun bindService(){
        intentService = Intent(this,MusicService::class.java)
        if(connection==null) {
            connection = object : ServiceConnection {
                override fun onServiceDisconnected(p0: ComponentName?) {
                    isBound = false
                }

                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    isBound = true
                    val binder:MusicService.MusicServiceBinder = p1 as MusicService.MusicServiceBinder
                    musicService = binder.getService()
                    presenter?.setService(musicService!!)
                    presenter?.onServiceConnected()
                    presenter?.setTimeSong()
                    Log.d(App.TAG,"connect service in ActivityPlay")
                }
            }
        }
        bindService(intentService,connection!!, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(){
        unbindService(connection!!)
    }

    fun close(){
        unbindService()
        presenter?.unregisterReceiver(this)
        presenter?.exitActiPlay()
    }

    fun updateSongInfo(song: Song){
        val cover = MyMedia(this).getCoverBitMap(song)
        txtTitleActiPlay.text = song.title
        txtArtistActiPlay.text = song.artist
        sbSongActiPlay.max = song.timeTotal
        imgCoverActiPlay.setImageBitmap(cover)
        txtTimeTotal.text = timeFormat.format(song.timeTotal)
        btnPlayActiPlay.setImageResource(R.drawable.ic_pause_2)
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnNextActiPlay -> presenter?.onNextSong()
            btnPlayActiPlay -> presenter?.onPauseSong()
            btnPrevActiPlay -> presenter?.onPrevSong()
        }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        progessSeekBar = p1
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        Log.d(App.TAG,"onStartTrackingTough: p0=$p0")
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        presenter?.onSeekTo(progessSeekBar)
    }

    override fun updateInfoSongNowActiPlay(song: Song) {
        updateSongInfo(song)
    }

    override fun updateStatusPLayActiPlay(icon: Int) {
        btnPlayActiPlay.setImageResource(icon)
    }

    override fun updateSeekbar(progress: Int) {
        sbSongActiPlay.progress = progress
        txtCurrentTime.text = timeFormat.format(progress)
        Log.d(App.TAG,"Seekbar set progress: $progress")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        close()
        Log.d(App.TAG,"onBackPressedActivityPlay")
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
        Log.d(App.TAG,"onDestroyActivityPlay")
    }

    override fun exitActivityPlay() {
        finish()
        Log.d(App.TAG,"onExitActivityPlay")
    }
}
