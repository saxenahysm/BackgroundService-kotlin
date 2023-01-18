package com.shyam.roomdbexample

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.shyam.roomdbexample.UtilsForBG.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(){
    private lateinit var bookDao: BookDao
    private val arrayList: ArrayList<Book> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.O)
    val current: String = LocalDateTime.now().format(formatter)

    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), 0
        )


        val db = Room.databaseBuilder(
            applicationContext, BookDatabase::class.java, "book_database"
        ).fallbackToDestructiveMigration().build()
        bookDao = db.bookDao()
//        testDB()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testDB() {

        lifecycleScope.launch(Dispatchers.IO) {
            //Insert
            Log.i("MyTAG", "*****     Inserting 3 Books     **********")
            bookDao.insertBook(Book(0, "Java", "Alex", current))
            bookDao.insertBook(Book(0, "PHP", "Mike", current))
            bookDao.insertBook(Book(0, "Kotlin", "Amelia", current))
            Log.i("MyTAG", "*****     Inserted 3 Books       **********")

            //Query
            val books = bookDao.getAllBook()
            Log.i("MyTAG", "*****   ${books.size} books there *****")
            for (book in books) {
                Log.i(
                    "MyTAG",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.created_at}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            //Insert
            Log.i("MyTAG", "*****     Inserting 3 Books     **********")
            bookDao.insertBook(Book(0, "Java", "Alex", current))
            bookDao.insertBook(Book(0, "PHP", "Mike", current))
            bookDao.insertBook(Book(0, "Kotlin", "Amelia", current))
            Log.i("MyTAG", "*****     Inserted 3 Books       **********")
        }
    }

    fun getAllData(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            //Query
            val books = bookDao.getAllBook()
            Log.i("MyTAG", "*****   ${books.size} books there *****")
            for (book in books) {
                Log.i(
                    "MyTAG",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.created_at}"
                )
                arrayList.add(book)
            }

        }
    }

    fun stopService(view: View) {

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
    }

    fun startService(view: View) {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

    private fun saveTrackingDetails(stringJson:String) = try {
        //https://tk.com/api/shyam/save-live-location-test
        val request = object : StringRequest(Request.Method.POST, "url",
            Response.Listener {
                android.util.Log.e("TAG111", "saveTrackingDetails: ${it.toString()}" )
        }, Response.ErrorListener {
                android.util.Log.e("TAG111", "saveTrackingDetails: ${it.toString()}" )
        }) {
            override fun getParams(): Map<String, String>? {
                val param = HashMap<String, String>()
                param["list"] = stringJson
                param["emp_id"] = "1234"
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

        saveTrackingDetails(stringJson)
    }

}
