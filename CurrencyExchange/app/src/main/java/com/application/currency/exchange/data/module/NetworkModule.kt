package com.application.currency.exchange.data.module

import android.content.Context
import android.net.ConnectivityManager
import com.application.currency.exchange.data.datasource.api.service.ExchangeRateService
import com.application.currency.exchange.data.datasource.api.util.NetworkUtil
import com.application.currency.exchange.data.repository.api.ApiRepository
import com.application.currency.exchange.data.repository.api.ApiRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideCurrencyExchangeRateService(retrofit: Retrofit): ExchangeRateService {
        return retrofit.create(ExchangeRateService::class.java)
    }

    @Provides
    @Singleton
    fun provideCurrencyExchangeRateRepository(service: ExchangeRateService,
                                              networkUtil: NetworkUtil): ApiRepository {
        return ApiRepositoryImpl(service, networkUtil)
    }

    @Provides
    @Singleton
    fun provideNetworkUtil(@ApplicationContext context: Context): NetworkUtil {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        return NetworkUtil(connectivityManager)
    }
}