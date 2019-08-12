package com.example.musicappdemo4

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappdemo4.data.SongAdapter
import com.example.musicappdemo4.data.model.MyMedia
import com.example.musicappdemo4.data.model.Song
import com.example.musicappdemo4.presenter.MediaContract
import com.example.musicappdemo4.presenter.MediaPresenter
import com.example.musicappdemo4.service.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), SongAdapter.SongClick, MediaContract.MainView {

    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123
    var intentService:Intent? = null
    var musicService: MusicService? = null
    var connection: ServiceConnection? = null
    var presenter: MediaContract.Presenter? = null
    var isBound = false
    var posSongNow=0
    val myMedia = MyMedia(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        presenter = MediaPresenter(this)
        presenter?.registerReceiver(this)
        connectService()
        btnPlayMain.setOnClickListener{
            presenter?.onPauseSong()
        }

        imgCoverMain.setOnClickListener{

        }
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

    @SuppressLint("WrongConstant")
    fun initRecycleView(){
        val listSong = MyMedia(this).getListSong()
        recycleViewMain.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,false)
        recycleViewMain.adapter = SongAdapter(listSong,this)
    }

    fun initCurrentSongView(song: Song){
        imgCoverMain.setImageBitmap(myMedia.getCoverBitMap(song))
        txtArtistMain.text = song.artist
        txtTitleMain.text = song.title
        btnPlayMain.setImageResource(R.drawable.ic_pause_main)
    }

    override fun onSongClick(index:Int) {
        //Toast.makeText(this,"song = $song",Toast.LENGTH_LONG).show()
//        initCurrentSongView(song)
//        musicService?.createNoti(song,true)
        presenter?.onPickSongToPlay(index)
        Log.d("DEMO123","song: $index")
    }

    override fun updateInfoSongNow(song: Song) {
        initCurrentSongView(song)
    }

    override fun updateStatusPlay(icon:Int) {
        //val icon = if(isPlay){R.drawable.ic_play_main} else{R.drawable.ic_pause_main}
        btnPlayMain.setImageResource(icon)
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
