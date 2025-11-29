package com.application.currency.exchange.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import com.application.currency.exchange.R

@Composable
fun CurrencyExchangeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(LocalResources.current.getColor(
                R.color.primary_color, null)),
            secondary = Color(LocalResources.current.getColor(
                R.color.theme_secondary, null)),
            tertiary = Color(LocalResources.current.getColor(
                R.color.theme_tertiary, null))),
        typography = Typography,
        content = content
    )
}