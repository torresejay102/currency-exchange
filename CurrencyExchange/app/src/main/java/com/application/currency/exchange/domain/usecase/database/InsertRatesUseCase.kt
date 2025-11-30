package com.application.currency.exchange.domain.usecase.database

import com.application.currency.exchange.data.repository.database.RateRepository
import com.application.currency.exchange.domain.entity.model.Rate
import javax.inject.Inject

class InsertRatesUseCase @Inject constructor(
    private val repository: RateRepository) {
    suspend operator fun invoke(vararg rate: Rate) = repository.insertRate(*rate)
}