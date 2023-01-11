package com.shyam.roomdbexample

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var bookDao: BookDao

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    val current = LocalDateTime.now().format(formatter)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val db = Room.databaseBuilder(
            applicationContext, BookDatabase::class.java, "book_database"
        ).build()
        bookDao = db.bookDao()
        testDB()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testDB() {

        lifecycleScope.launch(Dispatchers.IO) {
            //Insert
            Log.i("MyTAG", "*****     Inserting 3 Books     **********")
            bookDao.insertBook(Book(0, "Java", "Alex", current.toString()))
            bookDao.insertBook(Book(0, "PHP", "Mike", current.toString()))
            bookDao.insertBook(Book(0, "Kotlin", "Amelia", current.toString()))
            Log.i("MyTAG", "*****     Inserted 3 Books       **********")

            //Query
            val books = bookDao.getAllBook()
            Log.i("MyTAG", "*****   ${books.size} books there *****")
            for (book in books) {
                Log.i(
                    "MyTAG",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.time}"
                )
            }
/*
            //Update
            Log.i("MyTAG","*****      Updating a book      **********")
            bookDao.updateBook(Book(1,"PHP Updated","Mike"))
            //Query
            val books2 = bookDao.getAllBook()
            Log.i("MyTAG","*****   ${books2.size} books there *****")
            for(book in books2){
                Log.i("MyTAG","id: ${book.id} name: ${book.name} author: ${book.author}")
            }

            //delete
            Log.i("MyTAG","*****       Deleting a book      **********")
            bookDao.deleteBook(Book(1,"PHP","Mike"))
            val books3 = bookDao.getAllBook()
            for(book in books3){
                Log.i("MyTAG","id: ${book.id} name: ${book.name} author: ${book.author}")
            }*/
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData() {
        lifecycleScope.launch(Dispatchers.IO) {
            //Insert
            Log.i("MyTAG", "*****     Inserting 3 Books     **********")
            bookDao.insertBook(Book(0, "Java", "Alex", current.toString()))
            bookDao.insertBook(Book(0, "PHP", "Mike", current.toString()))
            bookDao.insertBook(Book(0, "Kotlin", "Amelia", current.toString()))
            Log.i("MyTAG", "*****     Inserted 3 Books       **********")
        }

    }

    fun getAllData() {
        lifecycleScope.launch(Dispatchers.IO) {
            //Query
            val books = bookDao.getAllBook()
            Log.i("MyTAG", "*****   ${books.size} books there *****")
            for (book in books) {
                Log.i(
                    "MyTAG",
                    "id: ${book.id} latitude: ${book.lat} Longitude: ${book.lng} time: ${book.time}"
                )
            }
        }
    }
}