package com.application.currency.exchange.data.datasource.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.application.currency.exchange.domain.entity.model.Rate

@Dao
interface RateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRate(vararg rate: Rate)

    @Delete
    suspend fun deleteRate(vararg rate: Rate)

    @Update
    suspend fun updateRate(vararg rate: Rate)

    @Query("SELECT * FROM table_rate")
    suspend fun getAllRates(): List<Rate>
}