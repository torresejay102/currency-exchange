package com.application.currency.exchange.domain.entity.model

data class Rate(val currency: String,
                val conversionMap: MutableMap<String, ConversionValue>,
                var amount: Float = 0f)
