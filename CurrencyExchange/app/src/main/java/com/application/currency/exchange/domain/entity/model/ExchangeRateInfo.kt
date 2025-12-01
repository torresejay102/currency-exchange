package com.application.currency.exchange.domain.entity.model

data class ExchangeRateInfo(val rates: List<Rate>, val sellRate: Rate? = null,
                            val receiveRate: Rate? = null, val sellValue: Double? = null,
                            val receiveValue: Double? = null)