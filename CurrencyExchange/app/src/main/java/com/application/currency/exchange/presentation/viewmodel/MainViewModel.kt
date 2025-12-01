package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.application.currency.exchange.data.datasource.api.util.NetworkUtil
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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrencyExchangeRateUseCase: GetExchangeRateUseCase,
    private val insertRatesUseCase: InsertRatesUseCase,
    private val updateRatesUseCase: UpdateRatesUseCase,
    private val getAllRatesUseCase: GetAllRatesUseCase,
    private val preferences: Preferences):
    BaseViewModel<MainScreenEvent, List<Rate>>(MainScreenState.None) {

        private val startingCurrency = "EUR"
        private val startingAmount = 1000.0

        private var sellValue = 0.0
        private var receiveValue = 0.0
        private lateinit var sellRate: Rate
        private lateinit var receiveRate: Rate
        private lateinit var rates: MutableList<Rate>

        private var hasExchangeRateInfoInit = false

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
                var rateList = mutableListOf<Rate>()

                if(!::rates.isInitialized)
                    rateList = getAllRatesUseCase.invoke().toMutableList()

                var baseRate = rateList.find { it.currency.lowercase() ==
                        currencyExchangeRate.base.lowercase() }

                if(baseRate == null) {
                    baseRate = Rate(currencyExchangeRate.base,
                        mutableMapOf())
                    rateList.add(baseRate)
                    if (baseRate.currency == startingCurrency)
                        baseRate.amount = startingAmount
                }

                list.forEach { key, value ->
                    val rate = rateList.find { it.currency.lowercase() == key.lowercase() } ?:
                        Rate(key, mutableMapOf())

                    if(rate.conversionMap.isEmpty() && baseRate.currency != rate.currency) {
                        rateList.add(rate)
                    }

                    val baseConversionValue = ConversionValue(1 / value.toDouble(),
                        currencyExchangeRate.date)
                    val conversionValue = ConversionValue(value.toDouble(),
                        currencyExchangeRate.date)

                    rate.conversionMap[baseRate.currency] = conversionValue
                    baseRate.conversionMap[rate.currency] = baseConversionValue
                }

                this@MainViewModel.rates = rateList.sortedBy { it.currency }.toMutableList()
                sendBackRates(event)
            }
        }

        private fun callExchangeRateService(event: MainScreenEvent) {
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                val result = getCurrencyExchangeRateUseCase.invoke()
                when(result) {
                    is NetworkResult.Error -> {
                        val errorCode = result.code
                        if(errorCode == NetworkUtil.CODE_NO_INTERNET_CONNECTION) {
                            var dbRates: MutableList<Rate>
                            if(!::rates.isInitialized) {
                                dbRates = getAllRatesUseCase.invoke()
                                    .sortedBy { it.currency }.toMutableList()
                            } else
                                dbRates = rates

                            if(dbRates.isNotEmpty()) {
                                rates = dbRates
                                if(!hasExchangeRateInfoInit) {
                                    resetExchangeRateInfo()
                                    hasExchangeRateInfoInit = true
                                }
                                _state.value = MainScreenState.Offline(ExchangeRateInfo(rates,
                                    sellRate, receiveRate, sellValue, receiveValue))
                            }
                            else
                                _state.value = MainScreenState.Offline(null)
                        }
                        else {
                            _state.value = MainScreenState.Error(
                                result.message.orEmpty()
                            )
                        }
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
                if(sellValue > sellRate.amount)
                    sellValue = sellRate.amount
                if(sellRate.currency == receiveRate.currency)
                    receiveRate = rates.find {
                        receiveRate.conversionMap.contains(sellRate.currency) &&
                                receiveRate.currency != sellRate.currency
                    } ?: rates[0]
                receiveRate.conversionMap[sellRate.currency]?.let {
                    receiveValue = sellValue * it.value
                    _state.value = MainScreenState.UIUpdated(event,
                        ExchangeRateInfo(rates, sellRate, receiveRate, sellValue, receiveValue))
                }
            }
            removeEvent()
        }

        private fun updateBalances() {
            if(sellValue == 0.0) {
                removeEvent()
                return
            }
            receiveRate.conversionMap[sellRate.currency]?.let {
                sellRate.amount -= sellValue
                receiveRate.amount += receiveValue

                val message = "You have converted ${formatDoubleToTwoDecimals(sellValue)} " +
                        "${sellRate.currency} to " + "${formatDoubleToTwoDecimals(
                    receiveValue)} ${receiveRate.currency}."

                resetExchangeRateInfo()

                if(formatDoubleToTwoDecimals(sellRate.amount).toDouble() == 0.0)
                    sellRate.amount = 0.0
                if(formatDoubleToTwoDecimals(receiveRate.amount).toDouble() == 0.0)
                    receiveRate.amount = 0.0

                _state.value = MainScreenState.BalanceUpdated(ExchangeRateInfo(rates, sellRate,
                    receiveRate, sellValue, receiveValue), message)
                removeEvent()
            }
        }

        private fun resetExchangeRateInfo() {
            val sellRates = rates.filter { it.amount > 0 }

            if(::sellRate.isInitialized) {
                if (sellRates.find { it.currency == sellRate.currency } == null)
                    sellRate = sellRates[0]
                if (sellValue > sellRate.amount)
                    sellValue = sellRate.amount
            }
            else {
                sellRate = sellRates[0]
                sellValue = sellRate.amount
            }

            val receiveRates = rates.filter { it.conversionMap.contains(sellRate.currency) }

            if(::receiveRate.isInitialized) {
                if (receiveRates.find { it.currency == receiveRate.currency  &&
                            it.currency != sellRate.currency} == null)
                    receiveRate = receiveRates[0]
            }
            else
                receiveRate = receiveRates[0]

            receiveValue = receiveRate.conversionMap[sellRate.currency]?.value?.let {
                it * sellValue
            } ?: 0.0
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

        private fun formatDoubleToTwoDecimals(number: Double): String {
            return "%.2f".format(number)
        }

        fun saveDatabaseDetails() {
            viewModelScope.launch(Dispatchers.IO) {
                insertRatesUseCase.invoke(*rates.toTypedArray())
            }
        }
}