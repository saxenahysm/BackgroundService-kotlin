package com.shyam.roomdbexample.RoomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shyam.roomdbexample.RoomDB.book.LocationModel
import com.shyam.roomdbexample.RoomDB.book.BookDao
import com.shyam.roomdbexample.RoomDB.user.User
import com.shyam.roomdbexample.RoomDB.user.UserDAO

@Database(entities = [LocationModel::class, User::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDAO
}