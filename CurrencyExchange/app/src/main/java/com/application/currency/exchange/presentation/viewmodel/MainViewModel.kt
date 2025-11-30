package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.application.currency.exchange.domain.entity.model.ConversionValue
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

                                val baseRate = Rate(currencyExchangeRate.base,
                                    mutableMapOf())
                                rates.add(baseRate)
                                if(baseRate.currency == "EUR")
                                    baseRate.amount = 1000f

                                list.forEach { key, value ->
                                    val rate = Rate(key, mutableMapOf())
                                    val conversionValue = ConversionValue(
                                        value.toFloat(), currencyExchangeRate.date)
                                    rate.conversionMap[baseRate.currency] = conversionValue
                                    baseRate.conversionMap[rate.currency] = conversionValue
                                    rates.add(rate)
                                }
                                this@MainViewModel.rates = rates.sortedBy { it.currency }
                                    .toMutableList()
                                _state.value = MainScreenState.Success(this@MainViewModel.rates)
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
}