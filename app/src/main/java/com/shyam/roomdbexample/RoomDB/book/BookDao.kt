package com.shyam.roomdbexample.RoomDB.book

import androidx.room.*
import com.shyam.roomdbexample.RoomDB.user.User

@Dao
interface BookDao {

    @Query("SELECT * FROM books_table")
    fun getAllLocations(): List<LocationModel>

    @Query("SELECT * FROM books_table WHERE created_at LIKE :date")
    fun getLocationForSelectedDate(date: String): List<LocationModel>

    @Query("DELETE FROM books_table")
    fun deleteAllLocations(): Int


    @Insert
    fun insertLocation(locationModel: LocationModel)

    @Delete
    fun deleteLocations(locationModel: LocationModel)


    @Update
    fun updateLocation(locationModel: LocationModel)
}