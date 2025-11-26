package com.application.currency.exchange.data.repository

import com.application.currency.exchange.data.datasource.api.handler.handleApi
import com.application.currency.exchange.data.datasource.api.service.CurrencyExchangeRateService
import com.application.currency.exchange.domain.entity.model.CurrencyExchangeRate
import com.application.currency.exchange.domain.entity.network.NetworkResult
import javax.inject.Inject

interface CurrencyExchangeRateRepository {
    suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate>
}

class CurrencyExchangeRateRepositoryImpl @Inject constructor(
    private val service: CurrencyExchangeRateService) : CurrencyExchangeRateRepository {
    override suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate> {
        return handleApi { service.getCurrencyExchangeRate() }
    }
}