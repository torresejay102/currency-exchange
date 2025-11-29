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
                            _state.value = MainScreenState.Error(result.message.orEmpty())
                        }
                        is NetworkResult.Exception -> {
                            _state.value = MainScreenState.Error(result.throwable.message.orEmpty())
                        }
                        is NetworkResult.Success -> {
                            val rates = mutableListOf<Rate>()
                            val currencyExchangeRate = result.data
                            val list = currencyExchangeRate.rates
                            list.forEach { key, value ->
                                rates.add(Rate(key, value.toFloat(),
                                    currencyExchangeRate.date,
                                    if(currencyExchangeRate.base == key) null
                                    else currencyExchangeRate.base))
                            }
                            _state.value = MainScreenState.Success(rates)
                        }
                    }
                }
            }
        }
    }
}