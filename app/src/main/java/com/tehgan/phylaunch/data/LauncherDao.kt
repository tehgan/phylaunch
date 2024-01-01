package com.tehgan.phylaunch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM home_db ORDER BY position")
    fun getAppList(): Flow<List<LauncherInfo>>

    @Query("SELECT * FROM home_db WHERE position >= :p0 AND position <= :p1 ORDER BY position")
    fun getPage(p0: Int, p1: Int): Flow<List<LauncherInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: LauncherInfo)

    @Update
    suspend fun update(app: LauncherInfo)

    @Delete
    suspend fun delete(app: LauncherInfo)
}
