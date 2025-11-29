package com.application.currency.exchange.presentation.event

sealed class MainScreenEvent: BaseScreenEvent() {
    data object OnGetExchangeRate: MainScreenEvent()
}