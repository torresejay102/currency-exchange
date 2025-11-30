package com.application.currency.exchange.domain.entity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_NAME = "table_rate"

@Entity(tableName = TABLE_NAME)
data class Rate(val currency: String,
                val conversionMap: MutableMap<String, ConversionValue>,
                var amount: Float = 0f,
                @PrimaryKey(autoGenerate = true) val id: Int = 0)
