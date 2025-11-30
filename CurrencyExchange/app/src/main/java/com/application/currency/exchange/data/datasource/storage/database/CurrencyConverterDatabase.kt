package com.application.currency.exchange.data.datasource.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.application.currency.exchange.data.datasource.storage.converter.ConversionMapConverter
import com.application.currency.exchange.data.datasource.storage.dao.RateDao
import com.application.currency.exchange.domain.entity.model.Rate
import kotlin.jvm.java

const val DATABASE_NAME = "CurrencyConverter"

@Database(entities = [Rate::class], version = 1, exportSchema = false)
@TypeConverters(ConversionMapConverter::class)
abstract class CurrencyConverterDatabase: RoomDatabase() {
    abstract fun rateDao(): RateDao

    companion object {
        @Volatile
        private var INSTANCE: CurrencyConverterDatabase? = null

        fun getDatabase(context: Context): CurrencyConverterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CurrencyConverterDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}