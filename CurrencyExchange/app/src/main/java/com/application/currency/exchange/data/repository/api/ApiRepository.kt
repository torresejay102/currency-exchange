package com.application.currency.exchange.data.repository.api

import com.application.currency.exchange.data.datasource.api.handler.handleApi
import com.application.currency.exchange.data.datasource.api.service.ExchangeRateService
import com.application.currency.exchange.data.datasource.api.util.NetworkUtil
import com.application.currency.exchange.domain.entity.model.CurrencyExchangeRate
import com.application.currency.exchange.domain.entity.network.NetworkResult
import javax.inject.Inject

interface ApiRepository {
    suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate>
}

class ApiRepositoryImpl @Inject constructor(private val service: ExchangeRateService,
    private val networkUtil: NetworkUtil) : ApiRepository {

        override suspend fun getCurrencyExchangeRate(): NetworkResult<CurrencyExchangeRate> {
            if(!networkUtil.isOnline())
                return NetworkResult.Error(code = NetworkUtil.CODE_NO_INTERNET_CONNECTION,
                    message = NetworkUtil.NO_INTERNET_CONNECTION)
            return handleApi { service.getCurrencyExchangeRate() }
        }
}