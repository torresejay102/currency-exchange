package com.application.currency.exchange

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.application.currency.exchange.domain.usecase.GetCurrencyExchangeRateUseCase
import com.application.currency.exchange.presentation.view.screen.MainView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var getCurrencyExchangeRateUseCase: GetCurrencyExchangeRateUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(window, resources.getColor(R.color.primary_color,
            null))
        setContent {
            MainView()
        }
    }

    // Updates the Status Bar Color
    private fun setStatusBarColor(window: Window, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(color)
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
        } else {
            window.statusBarColor = color
        }
    }
}