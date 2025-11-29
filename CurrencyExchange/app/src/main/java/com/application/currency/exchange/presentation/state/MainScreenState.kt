package com.application.currency.exchange.presentation.state

import com.application.currency.exchange.domain.entity.model.Rate

sealed class MainScreenState: BaseScreenState() {
    object None: MainScreenState()
    object Loading : MainScreenState()
    data class Error(val errorMessage: String) : MainScreenState()
    data class Success(val list: List<Rate>) : MainScreenState()
    object AutoRefreshLoading : MainScreenState()
    data class AutoRefreshSuccess(val list: List<Rate>) : MainScreenState()
    data class ReceiveValueUpdated(val list: List<Rate>, val sellValue: Float,
                                   val receiveValue: Float, val sellRate: Rate,
                                   val receiveRate: Rate): MainScreenState()
}