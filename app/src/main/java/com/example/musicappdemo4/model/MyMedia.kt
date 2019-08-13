package com.example.musicappdemo4.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import com.example.musicappdemo4.R
import com.example.musicappdemo4.model.Song
import java.lang.Exception

class MyMedia(val context:Context) {

    fun getListSong():ArrayList<Song>{
        var listSong = ArrayList<Song>()
        val projection = arrayOf("title","artist","_data","album_id","duration")
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "is_music != 0",
            null,
            "title ASC"
        )
        if(cursor!= null && cursor.moveToFirst()){
            do{
                val title = cursor.getString(cursor.getColumnIndex("title"))
                val artist = cursor.getString(cursor.getColumnIndex("artist"))
                val path = cursor.getString(cursor.getColumnIndex("_data"))
                val albumID = cursor.getString(cursor.getColumnIndex("album_id"))
                val timeTotal = cursor.getInt(cursor.getColumnIndex("duration"))
                listSong.add(Song(title, artist, path, timeTotal))
                //Log.d("DEMO123","$title + $artist + $path")
            }while (cursor.moveToNext())
        }
        return listSong
    }

    fun getCoverBitMap(song: Song):Bitmap{
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(song.path)
        var bm:Bitmap? = null
        try{
            val art = mmr.embeddedPicture
            bm = BitmapFactory.decodeByteArray(art,0,art.size)
        }catch(e: Exception){
            bm = context.resources.getDrawable(R.drawable.disc1).toBitmap()
        }
        return bm!!
    }
}