package com.application.currency.exchange.data.repository.api

import com.application.currency.exchange.data.datasource.api.handler.handleApi
import com.application.currency.exchange.data.datasource.api.service.ExchangeRateService
import com.application.currency.exchange.domain.entity.model.CurrencyExchangeRate
import com.application.currency.exchange.domain.entity.network.NetworkResult
import javax.inject.Inject

interface ApiRepository {
    suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate>
}

class ApiRepositoryImpl @Inject constructor(
    private val service: ExchangeRateService) : ApiRepository {
    override suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate> {
        return handleApi { service.getCurrencyExchangeRate() }
    }
}