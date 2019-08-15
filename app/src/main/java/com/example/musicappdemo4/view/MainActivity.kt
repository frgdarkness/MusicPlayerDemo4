package com.example.musicappdemo4.view

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappdemo4.R
import com.example.musicappdemo4.model.App
import com.example.musicappdemo4.model.SongAdapter
import com.example.musicappdemo4.model.MyMedia
import com.example.musicappdemo4.model.Song
import com.example.musicappdemo4.presenter.MainContract
import com.example.musicappdemo4.presenter.MainPresenter
import com.example.musicappdemo4.service.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_play.*

class MainActivity : AppCompatActivity(), SongAdapter.SongClick, MainContract.MainView, View.OnClickListener {

    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    var intentService:Intent? = null
    var musicService: MusicService? = null
    var connection: ServiceConnection? = null
    var presenter: MainContract.Presenter? = null
    var isBound = false
    val myMedia = MyMedia(this)
    var isPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        presenter = MainPresenter(this)
        presenter?.registerReceiver(this)
        connectService()
        btnPlayMain.setOnClickListener(this)
        imgCoverMain.setOnClickListener(this)
        txtTitleMain.setOnClickListener(this)
        txtArtistMain.setOnClickListener(this)

    }

    fun connectService(){
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
                }
            }
        }
        bindService(intentService,connection!!, Context.BIND_AUTO_CREATE)
        startService(intentService)
    }

    fun closeService(){
        presenter?.stop()
        unbindService(connection!!)
        isBound = false
        stopService(intentService)
        Log.d(App.TAG,"unbinded and stopped Service")
    }

    @SuppressLint("WrongConstant")
    fun initRecycleView(){
        val listSong = MyMedia(this).getListSong()
        recycleViewMain.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,false)
        recycleViewMain.adapter = SongAdapter(listSong, this)
    }

    fun initCurrentSongView(song: Song){
        imgCoverMain.setImageBitmap(myMedia.getCoverBitMap(song))
        txtArtistMain.text = song.artist
        txtTitleMain.text = song.title
        btnPlayMain.setImageResource(R.drawable.ic_pause_main)
        if(isPlay==false)
            isPlay = true
    }

    fun openActivityPlay(){
        if(isPlay)
            startActivity(Intent(this,ActivityPlay::class.java))
    }

    override fun onSongClick(index:Int) {
        presenter?.onPickSongToPlay(index)
        Log.d(App.TAG,"song: $index")
    }

    override fun updateInfoSongNow(song: Song) {
        initCurrentSongView(song)
    }

    override fun updateStatusPlay(icon:Int) {
        btnPlayMain.setImageResource(icon)
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnPlayMain -> presenter?.onPauseSong()
            else -> {
                if(isPlay)
                    startActivity(Intent(this,ActivityPlay::class.java))
            }
        }
    }

    override fun exitMain() {
        closeService()
        finish()
        Log.d(App.TAG,"exitMain")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isBound) unbindService(connection!!)
        presenter?.unregisterReceiver(this)
        Log.d(App.TAG,"onDestroyMain")

    }

    fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

        } else {
            initRecycleView()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initRecycleView()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
