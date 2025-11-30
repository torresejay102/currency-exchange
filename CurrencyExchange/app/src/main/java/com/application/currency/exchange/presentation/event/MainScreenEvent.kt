package com.application.currency.exchange.presentation.event

import com.application.currency.exchange.domain.entity.model.Rate

sealed class MainScreenEvent: BaseScreenEvent() {
    data object OnGetExchangeRate: MainScreenEvent()
    data class OnInitSellValue(val amount: Float): MainScreenEvent()
    data class OnInitReceiveValue(val amount: Float): MainScreenEvent()
    data class OnInitSellCurrency(val rate: Rate): MainScreenEvent()
    data class OnInitReceiveCurrency(val rate: Rate): MainScreenEvent()
    data class OnUpdateSellValue(val amount: Float): MainScreenEvent()
    data class OnUpdateSellCurrency(val rate: Rate): MainScreenEvent()
    data class OnUpdateReceiveCurrency(val rate: Rate): MainScreenEvent()
    data object OnUpdateBalance: MainScreenEvent()
    data object OnRefreshExchangeRate
}