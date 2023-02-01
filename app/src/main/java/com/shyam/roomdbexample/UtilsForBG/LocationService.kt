package com.shyam.roomdbexample.UtilsForBG

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.shyam.roomdbexample.R
import com.shyam.roomdbexample.RoomDB.AppDatabase
import com.shyam.roomdbexample.RoomDB.book.Book
import com.shyam.roomdbexample.RoomDB.book.BookDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val serviceScopeForRomm = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private lateinit var bookDao: BookDao
    lateinit var current: String
    var strStatus: String = "Null";
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        strStatus = "onCreate";
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "book_database"
        ).build()
        bookDao = db.bookDao()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
//        return super.onStartCommand(intent, flags, startId)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun start() {
        val notification =
            NotificationCompat.Builder(this, "location").setContentText("Location:$strStatus")
                .setContentTitle("Track-location-Test").setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient.getLocationUpdates(2000L)
            .catch { e -> Log.e("Tag11", "getLocationUpdates: ${e.message}") }.onEach { location ->
                val formatter: DateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                current = LocalDateTime.now().format(formatter)
                val lat = location.latitude.toString().take(8)
                val lng = location.longitude.toString().take(8)
                val updatedNotification = notification.setContentText("Location: ($lat,$lng)")
                notificationManager.notify(1, updatedNotification.build())
                Log.e("TAG111", "$lat - $lng - $current" )
                insertData(lat, lng)
            }.launchIn(serviceScope)
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(lat: String, lng: String) {
        //Insert
        bookDao.insertBook(Book(0, lat, lng, current))
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        strStatus = "onDestroy";
    }
}


