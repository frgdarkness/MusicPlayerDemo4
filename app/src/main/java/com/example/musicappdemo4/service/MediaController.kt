package com.example.musicappdemo4.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.example.musicappdemo4.data.model.MyMedia
import com.example.musicappdemo4.data.model.Song

class MediaController(service: MusicService) : MediaPlayerListener{

    private val context:Context
    private val musicService: MusicService? = service
    private val mediaPlayer = MediaPlayer()
    private val listSong = ArrayList<Song>()
    private var notiReceiver: NotiReceiver? = null
    private var posSongNow = 0



    init {
        context = musicService?.applicationContext!!
        listSong.addAll(MyMedia(context).getListSong())
        initMusicPlayer()
        registerReceiver()
        Log.d("DEMO123","init in MediaController")
    }

    fun initMusicPlayer(){
        mediaPlayer.reset()
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setDataSource(listSong[0].path)
        mediaPlayer.prepare()
        Log.d(MusicService.TAG,"init mediaPlayer, listsong: ${listSong.size} song")
    }

    override fun nextSong() {
        if(posSongNow == listSong.size-1)
            posSongNow = 0
        else
            posSongNow++
        playSong()
    }

    override fun prevSong() {
        if(posSongNow>0)
            posSongNow--
        else
            posSongNow=listSong.size-1
        playSong()
    }

    override fun playSongAtIndex(index: Int) {
        posSongNow = index
        playSong()
        Toast.makeText(musicService,"play song at $index",Toast.LENGTH_LONG).show()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun pauseSong() {
        if(mediaPlayer.isPlaying)
            mediaPlayer.pause()
        else
            mediaPlayer.start()
        callUpdateStatusPlay()
        musicService?.createNoti(listSong[posSongNow],mediaPlayer.isPlaying)
    }

    override fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    override fun getCurrentTime(): Int {
        return mediaPlayer.currentPosition
    }

    fun playSong(){
        val song = listSong[posSongNow]
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.path)
        mediaPlayer.prepare()
        mediaPlayer.start()
        musicService?.createNoti(song,true)
        callUpdateSongInfo()
        Log.d("DEMO123","playsongat : $posSongNow")
    }

    fun callUpdateSongInfo() {
        val intent = Intent(MusicService.ACTION_UPDATE_INFO_SONG)
        intent.putExtra(MusicService.SONG_VALUE,listSong[posSongNow])
        context.sendBroadcast(intent)
    }

    fun callUpdateStatusPlay(){
        val intent = Intent(MusicService.ACTION_UPDATE_STATUS_PLAY)
        intent.putExtra(MusicService.PLAY_STATUS,isPlaying())
        Log.d("DEMO123","call update status: ${isPlaying()}")
        context.sendBroadcast(intent)
    }

    fun registerReceiver(){
        notiReceiver = NotiReceiver()
        val filter = IntentFilter()
        filter.addAction(MusicService.ACTION_PREV)
        filter.addAction(MusicService.ACTION_PLAY)
        filter.addAction(MusicService.ACTION_NEXT)
        musicService?.registerReceiver(notiReceiver,filter)
    }

    private inner class NotiReceiver : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("DEMO123","Controller receiver: action = ${p1?.action}")
            when(p1?.action){
                MusicService.ACTION_PREV -> prevSong()
                MusicService.ACTION_PLAY -> pauseSong()
                MusicService.ACTION_NEXT -> nextSong()
            }
        }

    }

}