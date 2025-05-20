package com.example.travelbuddy.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DestinationDao {
    
    @Query("SELECT * FROM destinations ORDER BY name ASC")
    suspend fun getAllDestinations(): List<Destination>
    
    @Query("SELECT * FROM destinations WHERE id = :id")
    suspend fun getDestinationById(id: Long): Destination
    
    @Insert
    suspend fun insertDestination(destination: Destination): Long
    
    @Update
    suspend fun updateDestination(destination: Destination)
    
    @Delete
    suspend fun deleteDestination(destination: Destination)
}
