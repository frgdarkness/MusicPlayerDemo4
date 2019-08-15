package com.example.musicappdemo4.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.example.musicappdemo4.model.App
import com.example.musicappdemo4.model.MyMedia
import com.example.musicappdemo4.model.Song
import kotlin.concurrent.thread

class MediaController(service: MusicService) : MediaPlayerListener, MediaPlayer.OnCompletionListener{

    private val context:Context
    private val musicService: MusicService? = service
    private val mediaPlayer = MediaPlayer()
    private val listSong = ArrayList<Song>()
    private var notiReceiver: NotiReceiver? = null
    private var posSongNow = 0

    init {
        context = musicService?.applicationContext!!
        listSong.addAll(MyMedia(context).getListSong())
        mediaPlayer.setOnCompletionListener(this)
        initMusicPlayer()
        registerReceiver()
        Log.d(App.TAG,"init in MediaController")
    }

    fun initMusicPlayer(){
        mediaPlayer.reset()
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setDataSource(listSong[0].path)
        mediaPlayer.prepare()
        Log.d(App.TAG,"init mediaPlayer, listsong: ${listSong.size} song")
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
        //Toast.makeText(musicService,"play song at $index",Toast.LENGTH_LONG).show()
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

    override fun getSongCurrent():Song {
        return listSong[posSongNow]
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
        Log.d(App.TAG,"playsongat : $posSongNow")
    }

    fun callUpdateSongInfo() {
        val intent = Intent(App.ACTION_UPDATE_INFO_SONG)
        intent.putExtra(App.SONG_VALUE,listSong[posSongNow])
        context.sendBroadcast(intent)
    }

    fun callUpdateStatusPlay(){
        val intent = Intent(App.ACTION_UPDATE_STATUS_PLAY)
        intent.putExtra(App.PLAY_STATUS,isPlaying())
        Log.d(App.TAG,"call update status: ${isPlaying()}")
        context.sendBroadcast(intent)
    }

    fun exit(){
        musicService?.calcelNoti()
        musicService?.stopService()
        unregisterReceiver()
        mediaPlayer.stop()
        mediaPlayer.release()
        Log.d(App.TAG,"controller exit")
    }

    override fun onCompletion(p0: MediaPlayer?) {
        nextSong()
    }

    fun registerReceiver(){
        notiReceiver = NotiReceiver()
        val filter = IntentFilter()
        filter.addAction(App.ACTION_PREV)
        filter.addAction(App.ACTION_PLAY)
        filter.addAction(App.ACTION_NEXT)
        filter.addAction(App.ACTION_EXIT)
        musicService?.registerReceiver(notiReceiver,filter)
    }

    fun unregisterReceiver(){
        musicService?.unregisterReceiver(notiReceiver)
    }

    private inner class NotiReceiver : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d(App.TAG,"Controller receiver: action = ${p1?.action}")
            when(p1?.action){
                App.ACTION_PREV -> prevSong()
                App.ACTION_PLAY -> pauseSong()
                App.ACTION_NEXT -> nextSong()
                App.ACTION_EXIT -> exit()
            }
        }
    }

}
