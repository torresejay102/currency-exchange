package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.application.currency.exchange.data.datasource.storage.preference.Preferences
import com.application.currency.exchange.domain.entity.model.ConversionValue
import com.application.currency.exchange.domain.entity.model.CurrencyExchangeRate
import com.application.currency.exchange.domain.entity.model.Rate
import com.application.currency.exchange.domain.entity.network.NetworkResult
import com.application.currency.exchange.domain.usecase.api.GetExchangeRateUseCase
import com.application.currency.exchange.domain.usecase.database.GetAllRatesUseCase
import com.application.currency.exchange.domain.usecase.database.InsertRatesUseCase
import com.application.currency.exchange.domain.usecase.database.UpdateRatesUseCase
import com.application.currency.exchange.presentation.event.MainScreenEvent
import com.application.currency.exchange.presentation.state.MainScreenState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrencyExchangeRateUseCase: GetExchangeRateUseCase,
    private val insertRatesUseCase: InsertRatesUseCase,
    private val updateRatesUseCase: UpdateRatesUseCase,
    private val getAllRatesUseCase: GetAllRatesUseCase,
    private val preferences: Preferences):

    BaseViewModel<MainScreenEvent, List<Rate>>(MainScreenState.None) {
        private val startingCurrency = "EUR"
        private val startingAmount = 1000f

        private var sellValue = 0.0f
        private var receiveValue = 0.0f
        private lateinit var sellRate: Rate
        private lateinit var receiveRate: Rate
        private lateinit var rates: MutableList<Rate>

        private val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            throwable.printStackTrace()
            _state.value = MainScreenState.Error(throwable.message.orEmpty())
        }

        override fun onEvent(event: MainScreenEvent) {
            when(event) {
                is MainScreenEvent.OnGetExchangeRate -> {
                    _state.value = MainScreenState.Loading
                    callExchangeRateService()
                }
                is MainScreenEvent.OnInitSellValue -> {
                    sellValue = event.amount
                }
                is MainScreenEvent.OnInitReceiveValue -> {
                    receiveValue = event.amount
                }
                is MainScreenEvent.OnInitSellCurrency -> {
                    sellRate = event.rate
                }
                is MainScreenEvent.OnInitReceiveCurrency -> {
                    receiveRate = event.rate
                }
                is MainScreenEvent.OnUpdateSellValue -> {
                    sellValue = event.amount
                    updateRateValues()
                }
                is MainScreenEvent.OnUpdateSellCurrency -> {
                    sellRate = event.rate
                    updateRateValues()
                }
                is MainScreenEvent.OnUpdateReceiveCurrency -> {
                    receiveRate = event.rate
                    updateRateValues()
                }
                is MainScreenEvent.OnUpdateBalance -> {
                    updateBalances()
                }
            }
        }

        private fun updateExchangeRates(currencyExchangeRate: CurrencyExchangeRate) {
            viewModelScope.launch(Dispatchers.IO) {
                val list = currencyExchangeRate.rates
                val rates = getAllRatesUseCase.invoke().toMutableList()

                var baseRate = rates.find { it.currency.lowercase() ==
                        currencyExchangeRate.base.lowercase() }

                if(baseRate == null) {
                    baseRate = Rate(currencyExchangeRate.base,
                        mutableMapOf())
                    rates.add(baseRate)
                    if (baseRate.currency == startingCurrency)
                        baseRate.amount = startingAmount
                }

                list.forEach { key, value ->
                    val rate = rates.find { it.currency.lowercase() == key } ?:
                        Rate(key, mutableMapOf())

                    if(rate.conversionMap.isEmpty()) {
                        rates.add(rate)
                        viewModelScope.launch(Dispatchers.IO) {
                            insertRatesUseCase.invoke(rate)
                        }
                    }

                    val conversionValue = ConversionValue(value.toFloat(),
                        currencyExchangeRate.date)
                    rate.conversionMap[baseRate.currency] = conversionValue
                    baseRate.conversionMap[rate.currency] = conversionValue

                    viewModelScope.launch(Dispatchers.IO) {
                        updateRatesUseCase.invoke(rate)
                    }
                }

                this@MainViewModel.rates = rates.sortedBy { it.currency }
                    .toMutableList()
                _state.value = MainScreenState.Success(this@MainViewModel.rates)
            }
        }

        private fun callExchangeRateService() {
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                val result = getCurrencyExchangeRateUseCase.invoke()
                when(result) {
                    is NetworkResult.Error -> {
                        _state.value = MainScreenState.Error(
                            result.message.orEmpty())
                    }
                    is NetworkResult.Exception -> {
                        _state.value = MainScreenState.Error(
                            result.throwable.message.orEmpty())
                    }
                    is NetworkResult.Success -> {
                        val str = convertAnyToString(result.data)
                        val hash = calculateSHA256(str)
                        hash?.let {
                            preferences.saveGetExchangeRateResponseHash(it)
                        }
                        updateExchangeRates(result.data)
                    }
                }
            }
        }

        private fun updateRateValues() {
            if(::sellRate.isInitialized && ::receiveRate.isInitialized) {
                receiveRate.conversionMap[sellRate.currency]?.let {
                    receiveValue = sellValue * it.value
                    _state.value = MainScreenState.ReceiveValueUpdated(rates, sellValue,
                        receiveValue, sellRate, receiveRate)
                }
            }
        }

        private fun updateBalances() {
            receiveRate.conversionMap[sellRate.currency]?.let {
                sellRate.amount -= sellValue
                receiveRate.amount += receiveValue
                sellValue = sellRate.amount
                receiveValue = sellValue * it.value

                val sellIndex = rates.indexOfFirst { it.currency == sellRate.currency }
                val receiveIndex = rates.indexOfFirst { it.currency == receiveRate.currency }

                rates[sellIndex] = sellRate
                rates[receiveIndex] = receiveRate

                _state.value = MainScreenState.ReceiveValueUpdated(
                    rates, sellValue,
                    receiveValue, sellRate, receiveRate
                )
            }
        }

        private fun convertAnyToString(value: Any): String {
            val gson = Gson()
            return gson.toJson(value)
        }

        private fun calculateSHA256(data: String): String? {
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(data.toByteArray(charset("UTF-8")))
                val hexString = StringBuilder()
                for (b in hash) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                return hexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                return null
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                return null
            }
        }
}