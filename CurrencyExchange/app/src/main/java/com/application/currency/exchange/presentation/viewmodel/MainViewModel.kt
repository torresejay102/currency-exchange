package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.application.currency.exchange.domain.entity.model.Rate
import com.application.currency.exchange.domain.entity.network.NetworkResult
import com.application.currency.exchange.domain.usecase.GetCurrencyExchangeRateUseCase
import com.application.currency.exchange.presentation.event.MainScreenEvent
import com.application.currency.exchange.presentation.state.MainScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val getCurrencyExchangeRateUseCase:
                                        GetCurrencyExchangeRateUseCase):
    BaseViewModel<MainScreenEvent, List<Rate>>(MainScreenState.None) {
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
                                val rates = mutableListOf<Rate>()
                                val currencyExchangeRate = result.data
                                val list = currencyExchangeRate.rates
                                list.forEach { key, value ->
                                    rates.add(Rate(key, value.toFloat(),
                                        currencyExchangeRate.date,
                                        if(currencyExchangeRate.base == key) null
                                        else currencyExchangeRate.base,
                                        if(currencyExchangeRate.base == key) 1000f
                                        else 0f))
                                }
                                this@MainViewModel.rates = rates
                                _state.value = MainScreenState.Success(rates)
                            }
                        }
                    }
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

        private fun updateRateValues() {
            if(::sellRate.isInitialized && ::receiveRate.isInitialized) {
                receiveValue = sellValue * receiveRate.convertedValue
                _state.value = MainScreenState.ReceiveValueUpdated(rates, sellValue,
                    receiveValue, sellRate, receiveRate)
            }
        }

        private fun updateBalances() {
            println("RECEIVE VALUE: "+receiveValue)
            sellRate.amount -= sellValue
            receiveRate.amount += receiveValue
            sellValue = sellRate.amount
            receiveValue = sellValue * receiveRate.convertedValue

            val sellIndex = rates.indexOfFirst { it.currency == sellRate.currency }
            val receiveIndex = rates.indexOfFirst { it.currency == receiveRate.currency }

            rates[sellIndex] = sellRate
            rates[receiveIndex] = receiveRate

            _state.value = MainScreenState.ReceiveValueUpdated(rates, sellValue,
                receiveValue, sellRate, receiveRate)
        }
}