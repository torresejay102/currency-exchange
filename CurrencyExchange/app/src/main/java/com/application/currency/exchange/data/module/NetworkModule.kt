package com.application.currency.exchange.data.module

import com.application.currency.exchange.data.datasource.api.CurrencyExchangeRateService
import com.application.currency.exchange.data.repository.CurrencyExchangeRateRepository
import com.application.currency.exchange.data.repository.CurrencyExchangeRateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://developers.paysera.com/tasks/api/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCurrencyExchangeRateService(retrofit: Retrofit): CurrencyExchangeRateService {
        return retrofit.create(CurrencyExchangeRateService::class.java)
    }

    @Provides
    @Singleton
    fun provideCurrencyExchangeRateRepository(service: CurrencyExchangeRateService): CurrencyExchangeRateRepository {
        return CurrencyExchangeRateRepositoryImpl(service)
    }
}