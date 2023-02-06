package com.shyam.roomdbexample.RoomDB.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "books_table")
data class Book(

    @PrimaryKey(autoGenerate = true) var id: Int,
    var lat: String,
    var lng: String,
    var created_at:String
)