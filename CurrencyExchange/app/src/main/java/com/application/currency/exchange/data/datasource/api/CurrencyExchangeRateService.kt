package com.application.currency.exchange.data.datasource.api

import com.application.currency.exchange.domain.entity.CurrencyExchangeRate
import retrofit2.http.GET

interface CurrencyExchangeRateService {
    @GET("currency-exchange-rates")
    suspend fun getCurrencyExchangeRate(): CurrencyExchangeRate
}