package com.shyam.roomdbexample.RoomDB.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) var id :Int,
    var name:String
)
