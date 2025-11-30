package com.application.currency.exchange.domain.usecase.database

import com.application.currency.exchange.data.repository.database.RateRepository
import javax.inject.Inject

class GetAllRatesUseCase @Inject constructor(
    private val repository: RateRepository) {
    suspend operator fun invoke() = repository.getAllRates()
}