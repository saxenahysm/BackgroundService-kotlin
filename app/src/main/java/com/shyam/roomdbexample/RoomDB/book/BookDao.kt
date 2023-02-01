package com.shyam.roomdbexample.RoomDB.book

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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