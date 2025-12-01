package com.application.currency.exchange.presentation.event

import com.application.currency.exchange.domain.entity.model.Rate

sealed class MainScreenEvent: BaseScreenEvent() {
    data object OnGetExchangeRate: MainScreenEvent()
    data class OnUpdateSellValue(val amount: Double): MainScreenEvent()
    data class OnUpdateSellCurrency(val rate: Rate): MainScreenEvent()
    data class OnUpdateReceiveCurrency(val rate: Rate): MainScreenEvent()
    data object OnUpdateBalance: MainScreenEvent()
    data object OnRefreshExchangeRate: MainScreenEvent()
}