package com.application.currency.exchange.data.repository

import com.application.currency.exchange.data.datasource.api.CurrencyExchangeRateService
import com.application.currency.exchange.domain.entity.CurrencyExchangeRate
import javax.inject.Inject

interface CurrencyExchangeRateRepository {
    suspend fun getCurrencyExchangeRate(): CurrencyExchangeRate?
}

class CurrencyExchangeRateRepositoryImpl @Inject constructor(
    private val service: CurrencyExchangeRateService) : CurrencyExchangeRateRepository {
    override suspend fun getCurrencyExchangeRate(): CurrencyExchangeRate? {
        return service.getCurrencyExchangeRate()
    }
}