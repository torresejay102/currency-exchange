package com.application.currency.exchange.data.datasource.api.service

import retrofit2.Response
import retrofit2.http.GET

interface ExchangeRateService {
    @GET("currency-exchange-rates")
    suspend fun getCurrencyExchangeRate(): Response<Any>

}