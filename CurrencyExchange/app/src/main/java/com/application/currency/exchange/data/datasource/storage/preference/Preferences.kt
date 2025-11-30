package com.application.currency.exchange.data.datasource.storage.preference

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Preferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefName = "CurrencyConverterPreferences"
    private val prefKeyGetExchangeRateResponseHash =
        "pref_key_get_currency_exchange_rate_response_hash"

    private val sharedPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun saveGetExchangeRateResponseHash(hash: String) {
        saveString(prefKeyGetExchangeRateResponseHash, hash)
    }

    fun retrieveGetExchangeRateResponseHash(): String? {
        return sharedPref.getString(prefKeyGetExchangeRateResponseHash, null)
    }

    private fun saveString(prefKey: String, value: String) {
        sharedPref.edit {
            putString(prefKey, value)
        }
    }
}