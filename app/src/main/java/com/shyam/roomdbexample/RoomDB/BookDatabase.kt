package com.shyam.roomdbexample.RoomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shyam.roomdbexample.RoomDB.book.Book
import com.shyam.roomdbexample.RoomDB.book.BookDao
import com.shyam.roomdbexample.RoomDB.user.User
import com.shyam.roomdbexample.RoomDB.user.UserDAO

@Database(entities = [Book::class,User::class], version = 3)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDAO
}