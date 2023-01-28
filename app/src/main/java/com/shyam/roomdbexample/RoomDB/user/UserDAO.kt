package com.shyam.roomdbexample.RoomDB.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDAO {

    @Query("SELECT * FROM user_table")
    fun getAllUsers(): List<User>

    @Insert
    fun insertUser(user: User)
    @Query("DELETE FROM user_table")
    fun deleteUser():Int
}