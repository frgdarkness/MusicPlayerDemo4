package com.example.musicappdemo4.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import com.example.musicappdemo4.view.MainActivity
import com.example.musicappdemo4.R
import com.example.musicappdemo4.model.App
import com.example.musicappdemo4.model.MyMedia
import com.example.musicappdemo4.model.Song

class MusicService : Service(){

    private val mBinder = MusicServiceBinder()
    private val myMedia = MyMedia(this)

    private var mediaController: MediaController? = null
    private var notiManager: NotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null

    val handler = Handler()
    var runnable:Runnable? = null

    inner class MusicServiceBinder:Binder(){
        fun getService() = this@MusicService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mediaController = MediaController(this)
        mediaSession = MediaSessionCompat(this,"MusicPLayer")
        notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        createNotiChannel()
        Log.d(App.TAG,"onCreate")
    }

    override fun onDestroy() {
        if(runnable !=null)
            handler.removeCallbacks(runnable!!)
        super.onDestroy()
        Log.d(App.TAG,"Service destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun createNotiChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Notification"
            val descriptionText = "Notification for MusicPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(App.CHANNEL_ID, name, importance)
            channel.apply {
                description = descriptionText
            }
            notiManager?.createNotificationChannel(channel)
        }
        Log.d(App.TAG,"creatNotiChannel")
    }

    fun createNoti(song: Song, isPlay: Boolean){

        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingMain = PendingIntent.getActivity(this,0, mainIntent,0)

        val iconPlay = if(isPlay) { R.drawable.ic_pause_main} else {R.drawable.ic_play_main}
        val bm = myMedia.getCoverBitMap(song)
        val noti = androidx.core.app.NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setLargeIcon(bm)
            .setSmallIcon(R.drawable.ic_small_disc)
            .addAction(R.drawable.ic_prev_noti,"previos",createPendingIntent(App.ACTION_PREV))
            .addAction(iconPlay,"play/pause",createPendingIntent(App.ACTION_PLAY))
            .addAction(R.drawable.ic_next_noti,"next",createPendingIntent(App.ACTION_NEXT))
            .addAction(R.drawable.ic_close,"exit",createPendingIntent(App.ACTION_EXIT))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession?.sessionToken))
            //.setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingMain)
            .build()
        startForeground(App.NOTI_ID,noti)
        Log.d(App.TAG,"Create Noti")
    }

    fun calcelNoti(){
        stopForeground(true)
    }

    fun createPendingIntent(action:String) = PendingIntent.getBroadcast(
        this, 0, Intent(action), 0
    )

    fun show(){
        Toast.makeText(this,"service",Toast.LENGTH_SHORT).show()
    }

    fun stopService(){
        stopForeground(true)
        stopSelf()
    }

    fun getMediaController() = mediaController

}
