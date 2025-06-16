package com.example.pest_detection_app.RoomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT has_password FROM users WHERE id = :userId")
    suspend fun getHasPassword(userId: Int): Boolean?


    @Query("UPDATE users SET has_password = :hasPassword WHERE id = :userId")
    suspend fun updateHasPassword(userId: Int, hasPassword: Boolean)




}
