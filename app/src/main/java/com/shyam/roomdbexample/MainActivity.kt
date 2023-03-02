package com.shyam.roomdbexample

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.IntentSender.SendIntentException
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
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.shyam.roomdbexample.RoomDB.AppDatabase
import com.shyam.roomdbexample.RoomDB.book.BookDao
import com.shyam.roomdbexample.RoomDB.book.LocationModel
import com.shyam.roomdbexample.RoomDB.user.UserDAO
import com.shyam.roomdbexample.UtilsForBG.LocationService
import com.shyam.roomdbexample.UtilsForBG.MyBinder
import com.shyam.roomdbexample.mapscreens.ShowTrackHistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {
    private val sharedPrefFile = "kotlinSharedPreference"
    private lateinit var sharedPreferences: SharedPreferences;

    private lateinit var userDAO: UserDAO
    private lateinit var bookDao: BookDao
    private val arrayList: ArrayList<LocationModel> = ArrayList()
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
//        addDummyData()
//        sharedPreferenceProcess()
//        getLocationList()
    }

    private fun sharedPreferenceProcess() {
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("isFirstTime", true)
        editor.commit()
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


    private var locationArrayLists: ArrayList<LocationModel> = ArrayList()

    private fun getLocationList() {
        Log.e("TAG111", "getLocationList: " + sharedPreferences.getBoolean("isFirstTime",true))
        if (sharedPreferences.getBoolean("isFirstTime", false)) {
            var selectedDate: String = SimpleDateFormat("yyyy-MM-dd").format(Date())
            Log.e("TAG111", "selectedDate: " + selectedDate)

            progressBar = ProgressDialog(this)
            progressBar.setTitle("Please wait... ")
            progressBar.setMessage("Getting the location data")
            progressBar.show()
            locationArrayLists.clear()
            val request: StringRequest = object : StringRequest(Method.POST,
                "https://timekompas.com/api/shyam/getusertrackinghistory",
                Response.Listener { response: String ->
                    progressBar.hide()
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            Log.e("TAG111", "getLocationList: $response")
                            val jsonObject = JSONObject(response)
                            if (jsonObject.getString("status") == "true") {
                                if (jsonObject.getJSONArray("list_array").length() > 0) {
                                    val array = jsonObject.getJSONArray("list_array")
                                    for (i in 0 until array.length()) {
                                        val jsonArray = array.getJSONObject(i)
                                        bookDao.insertLocation(
                                            LocationModel(
                                                0,
                                                jsonArray.getString("lat"),
                                                jsonArray.getString("lng"),
                                                selectedDate
                                            )
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                Response.ErrorListener { volleyError: VolleyError ->
                    progressBar.hide()
                    volleyError.printStackTrace()
                }) {
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = java.util.HashMap()
                    params["empid"] = "10"
                    params["token_id"] = "10"
                    params["date"] = "2023-03-01"
                    Log.e("TAG111", "getLocationList: $params")
                    return params
                }
            }
            request.retryPolicy = DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(request)
        }
    }

    private fun addDummyData() {
        arrayList.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            bookDao.insertLocation(LocationModel(0, "21.2348842", "81.6036043", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348842", "81.6036156\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348134\n", "81.6036337\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348134\n", "81.603641\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348611\n", "81.6035442\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.234838\n", "81.6035234\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348308\n", "81.6035295\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348194\n", "81.6035211\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348207\n", "81.6035265\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348115\n", "81.6035216\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348152\n", "81.6035192\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348219\n", "81.6035275\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348227\n", "81.6035244\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348253\n", "81.6035286\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348268\n", "81.6035272\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348373\n", "81.6035297\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348519\n", "81.6035366\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348599\n", "81.6035402\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348645\n", "81.6035409\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348731\n", "81.6035445\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348798\n", "81.6035485\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348861\n", "81.6035523\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348949\n", "81.6035593\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349311\n", "81.6036216\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349311\n", "81.6036218\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349311\n", "81.6036221\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349312\n", "81.6036222\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349313\n", "81.6036224\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349316\n", "81.6036226\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349327\n", "81.6036231\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349351\n", "81.6036254\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349351\n", "81.6036254\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2349374\n", "81.6036269\n", "2023-03-02"))
            bookDao.insertLocation(LocationModel(0, "21.2348617\n", "81.6035664\n", "2023-03-02"))
        }
    }

    private fun getData() {
        arrayList.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            //Query
            val books = bookDao.getAllLocations()
            for (book in books) {
                Log.i(
                    "TAG11",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.created_at}"
                )
                arrayList.add(book)
            }
        }
        Toast.makeText(this, "Ready to save data into server", Toast.LENGTH_LONG).show()
    }

    fun deleteAllData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = userDAO.deleteUser()
            val books = bookDao.deleteAllLocations()
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
        progressBar = ProgressDialog(this);
        progressBar.setTitle("Please wait")
        progressBar.setCancelable(false)
        progressBar.show()
        val request = object : StringRequest(
            Method.POST,
            resources.getString(R.string.url),
            Response.Listener {
                progressBar.dismiss()
                try {
                    val jsonObject = JSONObject(it)
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show()
                    Log.e("TAG111", "saveTrackingDetails: $it")
                    if (jsonObject.getString("status").equals("true")) {
//                        deleteAllData()
                    }
                } catch (e: Exception) {
                }
            },
            Response.ErrorListener {
                progressBar.dismiss()
                Log.e("TAG111", "saveTrackingDetails: $it")
            }) {
            override fun getParams(): Map<String, String> {
                val param = HashMap<String, String>()
                param["emp_id"] = "10"
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

    lateinit var progressBar: ProgressDialog
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

        startActivity(Intent(applicationContext, ShowTrackHistoryActivity::class.java).apply { })
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
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
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