package com.example.musicappdemo4.service

import com.example.musicappdemo4.model.Song

interface MediaPlayerListener {

    fun nextSong()

    fun prevSong()

    fun pauseSong()

    fun playSongAtIndex(index: Int)

    fun isPlaying():Boolean

    fun getSongCurrent():Song

    fun seekTo(position: Int)

    fun getCurrentTime():Int

    //fun callUpdateSongInfo()





}