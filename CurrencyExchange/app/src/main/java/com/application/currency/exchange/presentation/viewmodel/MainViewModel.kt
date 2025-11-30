package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.application.currency.exchange.data.datasource.storage.preference.Preferences
import com.application.currency.exchange.domain.entity.model.ConversionValue
import com.application.currency.exchange.domain.entity.model.CurrencyExchangeRate
import com.application.currency.exchange.domain.entity.model.ExchangeRateInfo
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
import kotlin.math.round

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

        private var hasExchangeRateInfoInit = false
        private val eventList = mutableListOf<MainScreenEvent>()

        private val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            throwable.printStackTrace()
            _state.value = MainScreenState.Error(throwable.message.orEmpty())
        }

        override fun onEvent(event: MainScreenEvent) {
            when(event) {
                is MainScreenEvent.OnGetExchangeRate -> {
                    _state.value = MainScreenState.Loading
                    callExchangeRateService(event)
                }
                is MainScreenEvent.OnUpdateSellValue -> {
                    sellValue = event.amount
                    updateRateValues(event)
                }
                is MainScreenEvent.OnUpdateSellCurrency -> {
                    sellRate = event.rate
                    updateRateValues(event)
                }
                is MainScreenEvent.OnUpdateReceiveCurrency -> {
                    receiveRate = event.rate
                    updateRateValues(event)
                }
                is MainScreenEvent.OnUpdateBalance -> {
                    updateBalances()
                }
                is MainScreenEvent.OnRefreshExchangeRate -> {
                    _state.value = MainScreenState.AutoRefreshLoading
                    callExchangeRateService(event)
                }
            }
        }

        private fun updateExchangeRates(currencyExchangeRate: CurrencyExchangeRate,
                                        event: MainScreenEvent) {
            viewModelScope.launch(Dispatchers.IO) {
                val list = currencyExchangeRate.rates
                val rates = getAllRatesUseCase.invoke().toMutableList()

                var baseRate = rates.find { it.currency.lowercase() ==
                        currencyExchangeRate.base.lowercase() }
                val isBaseRateInserted = baseRate != null

                if(baseRate == null) {
                    baseRate = Rate(currencyExchangeRate.base,
                        mutableMapOf())
                    rates.add(baseRate)
                    if (baseRate.currency == startingCurrency)
                        baseRate.amount = startingAmount
                }

                list.forEach { key, value ->
                    val rate = rates.find { it.currency.lowercase() == key.lowercase() } ?:
                        Rate(key, mutableMapOf())

                    if(rate.conversionMap.isEmpty() && baseRate.currency != rate.currency) {
                        rates.add(rate)
                        viewModelScope.launch(Dispatchers.IO) {
                            insertRatesUseCase.invoke(rate)
                        }
                    }

                    val baseConversionValue = ConversionValue(1 / value.toFloat(),
                        currencyExchangeRate.date)
                    val conversionValue = ConversionValue(value.toFloat(),
                        currencyExchangeRate.date)

                    rate.conversionMap[baseRate.currency] = conversionValue
                    baseRate.conversionMap[rate.currency] = baseConversionValue

                    if(baseRate.currency != rate.currency) {
                        viewModelScope.launch(Dispatchers.IO) {
                            updateRatesUseCase.invoke(rate)
                        }
                    }
                }

                viewModelScope.launch(Dispatchers.IO) {
                    if(isBaseRateInserted)
                        updateRatesUseCase.invoke(baseRate)
                    else
                        insertRatesUseCase.invoke(baseRate)
                }

                this@MainViewModel.rates = rates.sortedBy { it.currency }.toMutableList()
                sendBackRates(event)
            }
        }

        private fun callExchangeRateService(event: MainScreenEvent) {
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                val result = getCurrencyExchangeRateUseCase.invoke()
                when(result) {
                    is NetworkResult.Error -> {
                        _state.value = MainScreenState.Error(
                            result.message.orEmpty())
                        removeEvent()
                    }
                    is NetworkResult.Exception -> {
                        _state.value = MainScreenState.Error(
                            result.throwable.message.orEmpty())
                        removeEvent()
                    }
                    is NetworkResult.Success -> {
                        val str = convertAnyToString(result.data)
                        val hash = calculateSHA256(str)
                        hash?.let {
                            val localHash = preferences.retrieveGetExchangeRateResponseHash()
                            if(localHash == it) {
                                if(!::rates.isInitialized)
                                    rates = getAllRatesUseCase.invoke()
                                        .sortedBy { it.currency }.toMutableList()
                                sendBackRates(event)
                            }
                            else {
                                preferences.saveGetExchangeRateResponseHash(it)
                                updateExchangeRates(result.data, event)
                            }
                        } ?: run {
                            updateExchangeRates(result.data, event)
                        }
                    }
                }
            }
        }

        private fun updateRateValues(event: MainScreenEvent) {
            if(::sellRate.isInitialized && ::receiveRate.isInitialized) {
                receiveRate.conversionMap[sellRate.currency]?.let {
                    receiveValue = round(sellValue * it.value * 100) / 100
                    _state.value = MainScreenState.UIUpdated(event,
                        ExchangeRateInfo(rates, sellRate, receiveRate, sellValue, receiveValue))
                }
            }
            removeEvent()
        }

        private fun updateBalances() {
            if(sellValue == 0f) {
                removeEvent()
                return
            }
            receiveRate.conversionMap[sellRate.currency]?.let {
                val prevSellValue = sellValue
                val prevReceiveValue = receiveValue

                sellRate.amount -= sellValue
                receiveRate.amount += receiveValue
                sellValue = sellRate.amount
                receiveValue = sellValue * it.value

                viewModelScope.launch(Dispatchers.IO) {
                    updateRatesUseCase.invoke(receiveRate.currency,
                        receiveRate.amount)
                    updateRatesUseCase.invoke(sellRate.currency,
                        sellRate.amount)
                }

                val message = "You have converted $prevSellValue ${sellRate.currency} to " +
                        "$prevReceiveValue ${receiveRate.currency}."

                resetExchangeRateInfo()

                _state.value = MainScreenState.BalanceUpdated(ExchangeRateInfo(rates, sellRate,
                    receiveRate, sellValue, receiveValue), message)
                removeEvent()
            }
        }

        private fun resetExchangeRateInfo() {
            sellRate = rates.filter { it.amount > 0 }[0]
            sellValue = sellRate.amount

            val receiveRates = rates.filter { it.conversionMap.contains(sellRate.currency) }
            receiveRate = receiveRates[0]
            receiveValue = receiveRate.conversionMap[sellRate.currency]?.value?.let {
                round(it * sellValue * 100) / 100
            } ?: 0f
        }

        private fun sendBackRates(event: MainScreenEvent) {
            if(::rates.isInitialized) {
                if(!hasExchangeRateInfoInit) {
                    resetExchangeRateInfo()
                    hasExchangeRateInfoInit = true
                }
                if (event is MainScreenEvent.OnGetExchangeRate)
                    _state.value = MainScreenState.Success(rates)
                else if (event is MainScreenEvent.OnRefreshExchangeRate)
                    _state.value = MainScreenState.AutoRefreshSuccess(ExchangeRateInfo(rates,
                        sellRate, receiveRate, sellValue, receiveValue))
            }
            removeEvent()
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