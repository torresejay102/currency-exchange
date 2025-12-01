package com.application.currency.exchange.data.repository.database

import com.application.currency.exchange.data.datasource.storage.dao.RateDao
import com.application.currency.exchange.domain.entity.model.Rate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

interface RateRepository {
    fun insertRate(vararg rate: Rate)
    fun deleteRate(vararg rate: Rate)
    fun updateRate(vararg rate: Rate)
    fun updateRateAmount(currency: String, amount: Double)
    suspend fun getAllRates(): List<Rate>
}

class RateRepositoryImpl @Inject constructor(private val rateDao: RateDao) : RateRepository {
    override fun insertRate(vararg rate: Rate) {
        CoroutineScope(Dispatchers.IO).launch {
            rateDao.insertRate(*rate)
        }
    }

    override fun deleteRate(vararg rate: Rate) {
        CoroutineScope(Dispatchers.IO).launch {
            rateDao.deleteRate(*rate)
        }
    }

    override fun updateRate(vararg rate: Rate) {
        CoroutineScope(Dispatchers.IO).launch {
            rateDao.updateRate(*rate)
        }
    }

    override fun updateRateAmount(currency: String, amount: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            rateDao.updateRateAmount(currency, amount)
        }
    }

    override suspend fun getAllRates(): List<Rate> {
        return CoroutineScope(Dispatchers.IO).async {
            rateDao.getAllRates()
        }.await()
    }
}