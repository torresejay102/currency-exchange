package com.application.currency.exchange.domain.entity.model

data class Rate(val currency: String,
                val convertedValue: Float,
                val date: String,
                val baseCurrency: String? = null,
                val amount: Int = 0)
