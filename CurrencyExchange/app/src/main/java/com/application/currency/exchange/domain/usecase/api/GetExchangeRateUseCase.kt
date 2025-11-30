package com.application.currency.exchange.domain.usecase.api

import com.application.currency.exchange.data.repository.api.ApiRepository
import javax.inject.Inject

class GetExchangeRateUseCase @Inject constructor(
    private val repository: ApiRepository
) {
    suspend operator fun invoke() = repository.getCurrencyExchangeRate()
}