package com.application.currency.exchange.presentation.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.application.currency.exchange.R
import com.application.currency.exchange.presentation.event.MainScreenEvent
import com.application.currency.exchange.presentation.state.MainScreenState
import com.application.currency.exchange.presentation.view.content.MainScreenContent
import com.application.currency.exchange.ui.theme.CurrencyExchangeTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(state: MainScreenState, onEvent: (MainScreenEvent) -> Unit) {
    LaunchedEffect(onEvent) {
        onEvent(MainScreenEvent.OnGetExchangeRate)
    }
    CurrencyExchangeTheme {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.primary_color),
                        titleContentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    title = {
                        Text(modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.currency_converter),
                            textAlign = TextAlign.Center)
                    },
                    modifier = Modifier.background(
                        colorResource(R.color.primary_color)
                    ),
                    windowInsets = androidx.compose.foundation.layout.
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
            }
        ) { innerPadding ->
            MainScreenContent(state, onEvent,innerPadding)
        }
    }
}