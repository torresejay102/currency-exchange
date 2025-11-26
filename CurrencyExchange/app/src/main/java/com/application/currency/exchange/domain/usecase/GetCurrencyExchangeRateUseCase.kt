package com.application.currency.exchange.domain.usecase

import com.application.currency.exchange.data.repository.CurrencyExchangeRateRepository
import javax.inject.Inject

class GetCurrencyExchangeRateUseCase @Inject constructor(
    private val repository: CurrencyExchangeRateRepository) {
    suspend operator fun invoke() = repository.getCurrencyExchangeRate()
}