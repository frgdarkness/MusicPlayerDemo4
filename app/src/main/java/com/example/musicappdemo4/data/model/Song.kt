package com.example.musicappdemo4.data.model

import android.graphics.Bitmap
import java.io.Serializable

class Song (var title:String, var artist:String, var path:String, val timeTotal:Int):Serializable {
}