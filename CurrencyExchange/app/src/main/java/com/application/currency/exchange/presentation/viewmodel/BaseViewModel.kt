package com.application.currency.exchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.application.currency.exchange.presentation.event.BaseScreenEvent
import com.application.currency.exchange.presentation.state.BaseScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


abstract class BaseViewModel<T: BaseScreenEvent, V: Any>(initialState: BaseScreenState): ViewModel() {
    abstract fun onEvent(event: T)

    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<BaseScreenState> = _state
}
