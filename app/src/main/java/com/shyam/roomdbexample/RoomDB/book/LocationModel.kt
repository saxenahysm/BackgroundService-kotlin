package com.shyam.roomdbexample.RoomDB.book

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books_table")
data class LocationModel(

    @PrimaryKey(autoGenerate = true) var id: Int,
    var lat: String,
    var lng: String,
    var created_at:String
)
