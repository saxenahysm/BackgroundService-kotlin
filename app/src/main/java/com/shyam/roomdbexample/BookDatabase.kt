package com.shyam.roomdbexample

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Book::class], version = 2)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
}