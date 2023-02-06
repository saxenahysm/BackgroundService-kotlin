package com.shyam.roomdbexample

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.shyam.roomdbexample.RoomDB.AppDatabase
import com.shyam.roomdbexample.RoomDB.book.Book
import com.shyam.roomdbexample.RoomDB.book.BookDao
import com.shyam.roomdbexample.RoomDB.user.UserDAO
import com.shyam.roomdbexample.UtilsForBG.LocationService
import com.shyam.roomdbexample.UtilsForBG.MyBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private lateinit var userDAO: UserDAO
    private lateinit var bookDao: BookDao
    private val arrayList: ArrayList<Book> = ArrayList()
    var locationService = LocationService;

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.O)
    val current: String = LocalDateTime.now().format(formatter)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "book_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_3_4).build()
        userDAO = db.userDao()
        bookDao = db.bookDao()
        //testDB()
        if (checkPermissions()) enableLoc() else requestPermissions()
    }

    val MIGRATION_1_2: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }

    private val m_serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            locationService = (service as MyBinder).getService()
            Log.e("TAG", "onServiceConnected: " )
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.e("TAG111", "onServiceDisconnected: ")
            locationService = null!!
        }
    }

    fun getAllData(view: View) {
        getData()
    }

    private fun getData() {
        arrayList.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            //Query
            val books = bookDao.getAllBook()
            for (book in books) {
                Log.i(
                    "MyTAG",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.created_at}"
                )
                arrayList.add(book)
            }
        }
    }

    fun deleteAllData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = userDAO.deleteUser()
            val books = bookDao.deleteAllBook()
            Log.i("MyTAG", "*****   $user ITEMs there *****")
            Log.i("MyTAG", "*****   $books ITEMs there *****")
        }
        Toast.makeText(this, "History cleared...`:)", Toast.LENGTH_LONG).show()
    }

    fun stopService(view: View) {

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
        Toast.makeText(this, "Stopped", Toast.LENGTH_LONG).show()
    }

    fun startService(view: View) {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            bindService(intent, m_serviceConnection, BIND_AUTO_CREATE);
            startService(this)
        }
        Toast.makeText(this, "Tracking-started", Toast.LENGTH_LONG).show()
    }

    private fun saveTrackingDetails(stringJson: String) = try {
        val request = object : StringRequest(Method.POST,
            resources.getString(R.string.url),
            Response.Listener {
                try {
                    val jsonObject = JSONObject(it)
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show()
                    Log.e("TAG111", "saveTrackingDetails: $it")
                    if (jsonObject.getString("status").equals("true")) {
                        deleteAllData()
                    }
                } catch (e: Exception) {
                }
            },
            Response.ErrorListener {
                android.widget.Toast.makeText(this, it.message, android.widget.Toast.LENGTH_LONG)
                    .show()
                Log.e("TAG111", "saveTrackingDetails: $it")
            }) {
            override fun getParams(): Map<String, String> {
                val param = HashMap<String, String>()
                param["emp_id"] = "297"
                param["list"] = stringJson
                Log.e("TAG111", "getParams: $param")
                return param
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun saveDataIntoServer(view: View) {
        val gson = Gson()
        val stringJson = gson.toJson(arrayList)
        Log.e("TAG111", "arrayList: ${arrayList.size}")
        Log.e("TAG111", "stringJson: $stringJson")
        if (arrayList.size > 0) {
            saveTrackingDetails(stringJson)
        } else {
            Toast.makeText(this, "No records to save", Toast.LENGTH_LONG).show()
        }
    }

    private var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199

    private fun enableLoc() {
        if (googleApiClient == null) {
            googleApiClient =
                GoogleApiClient.Builder(this@MainActivity).addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(bundle: Bundle?) {
                            Log.e("TAG11", "onConnected: " )

                        }

                        override fun onConnectionSuspended(i: Int) {
                            googleApiClient!!.connect()
                            Log.e("TAG11", "onConnectionSuspended: ")
                        }
                    }).addOnConnectionFailedListener { connectionResult ->
                        Log.d("Location error", "Location error " + connectionResult.errorCode)
                    }.build()
            googleApiClient!!.connect()
            val locationRequest = LocationRequest.create()
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 0
            locationRequest.fastestInterval = 0

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val result: PendingResult<LocationSettingsResult> =
                LocationServices.SettingsApi.checkLocationSettings(
                    googleApiClient!!, builder.build()
                )
            result.setResultCallback(object : ResultCallback<LocationSettingsResult?> {
                override fun onResult(result: LocationSettingsResult) {
                    val status: Status = result.status
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.e("TAG11", "onResult: " )
                            status.startResolutionForResult(this@MainActivity, REQUEST_LOCATION)
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            })
        }
    }

    fun deleteAllData(view: View) {

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_RESTART
            startService(this)
        }
        Toast.makeText(this, "", Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), 0
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableLoc()
            } else {
                Toast.makeText(this, "Denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}
