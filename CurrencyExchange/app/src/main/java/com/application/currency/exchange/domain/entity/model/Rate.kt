package com.application.currency.exchange.domain.entity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_NAME = "table_rate"

@Entity(tableName = TABLE_NAME)
data class Rate(val currency: String,
                val conversionMap: MutableMap<String, ConversionValue>,
                var amount: Double = 0.0,
                @PrimaryKey(autoGenerate = true) val id: Int = 0)
