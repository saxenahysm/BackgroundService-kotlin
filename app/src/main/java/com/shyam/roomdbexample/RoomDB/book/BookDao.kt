package com.shyam.roomdbexample.RoomDB.book

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {

    @Query("SELECT * FROM books_table")
    fun getAllBook(): List<Book>

    @Query("DELETE FROM books_table")
    fun deleteAllBook(): Int

    @Insert
    fun insertBook(book: Book)

    @Delete
    fun deleteBook(book: Book)


    @Update
    fun updateBook(book: Book)
}