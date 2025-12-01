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

    protected val events = mutableListOf<T>()

    fun queueEvent(event: T) {
        events.add(event)
        if(events.size == 1)
            dequeueEvent()
    }

    fun dequeueEvent() {
        if(events.isEmpty())
            return
        onEvent(events[0])
    }

    fun removeEvent() {
        events.removeAt(0)
        dequeueEvent()
    }
}
