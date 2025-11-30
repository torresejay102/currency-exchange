package com.application.currency.exchange.data.datasource.storage.converter

import androidx.room.TypeConverter
import com.application.currency.exchange.domain.entity.model.ConversionValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConversionMapConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromString(value: String): MutableMap<String, ConversionValue> {
            val mapType = object : TypeToken<Map<String, ConversionValue>>() {}.type
            return Gson().fromJson(value, mapType)
        }

        @TypeConverter
        @JvmStatic
        fun fromMap(map: MutableMap<String, ConversionValue>): String {
            val gson = Gson()
            return gson.toJson(map)
        }
    }
}