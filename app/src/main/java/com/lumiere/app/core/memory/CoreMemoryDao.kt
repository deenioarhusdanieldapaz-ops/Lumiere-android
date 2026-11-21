package com.lumiere.app.core.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoreMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(memory: CoreMemory)
    
    @Query("SELECT * FROM core_memory WHERE `key` = :key")
    suspend fun get(key: String): CoreMemory?
    
    @Query("DELETE FROM core_memory WHERE expiresAt IS NOT NULL AND expiresAt < :now")
    suspend fun deleteExpired(now: Long)
}
