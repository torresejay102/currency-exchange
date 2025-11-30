package com.application.currency.exchange.data.module

import android.content.Context
import com.application.currency.exchange.data.datasource.storage.dao.RateDao
import com.application.currency.exchange.data.datasource.storage.database.CurrencyConverterDatabase
import com.application.currency.exchange.data.datasource.storage.preference.Preferences
import com.application.currency.exchange.data.repository.database.RateRepository
import com.application.currency.exchange.data.repository.database.RateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideRateRepository(rateDao: RateDao): RateRepository {
        return RateRepositoryImpl(rateDao)
    }

    @Provides
    @Singleton
    fun provideRateDao(@ApplicationContext context: Context): RateDao {
        return CurrencyConverterDatabase.getDatabase(context).rateDao()
    }

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): Preferences {
        return Preferences(context)
    }
}