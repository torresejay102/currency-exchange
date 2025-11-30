package com.application.currency.exchange.presentation.view.content

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.currency.exchange.R
import com.application.currency.exchange.domain.entity.model.ExchangeRateInfo
import com.application.currency.exchange.domain.entity.model.Rate
import com.application.currency.exchange.presentation.event.MainScreenEvent
import com.application.currency.exchange.presentation.state.MainScreenState
import com.application.currency.exchange.presentation.view.custom.ProgressDialog
import com.kanyidev.searchable_dropdown.DropdownMenuItem
import com.kanyidev.searchable_dropdown.LargeSearchableDropdownMenu
import kotlinx.coroutines.delay
import kotlin.math.round

@Composable
fun MainScreenContent(state: MainScreenState, queueEvent: (MainScreenEvent) -> Unit,
                      paddingValues: PaddingValues? = null) {

    val autoRefreshInterval = 5000L
    val balanceRates = remember { mutableStateListOf<Rate>() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(autoRefreshInterval)
            queueEvent(MainScreenEvent.OnRefreshExchangeRate)
        }
    }

    CurrencyConverterContent(paddingValues, queueEvent, state,balanceRates)
}

@Composable
fun CurrencyConverterContent(paddingValues: PaddingValues?, onEvent: (MainScreenEvent) -> Unit,
                             state: MainScreenState, balanceRatesSnapShot: SnapshotStateList<Rate>) {
    val headerFontSize = 20.sp

    var info by remember { mutableStateOf<ExchangeRateInfo?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if(state is MainScreenState.Success)
        info = ExchangeRateInfo(state.list)
    else if(state is MainScreenState.UIUpdated)
        info = state.info
    else if(state is MainScreenState.AutoRefreshSuccess)
        info = state.info
    else if(state is MainScreenState.BalanceUpdated) {
        info = state.info
        dialogMessage = state.message
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(paddingValues ?: PaddingValues(0.dp))) {

        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .height(40.dp)
                .padding(top = 0.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(state is MainScreenState.AutoRefreshLoading) {
                Text("Refreshing".uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color.Red)
                        .padding(top = 5.dp, bottom = 5.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontWeight = FontWeight.Medium),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(PaddingValues(20.dp))
                .imePadding()
        ) {
            info?.let {
                val rates = it.rates
                balanceRatesSnapShot.clear()
                balanceRatesSnapShot.addAll(rates.filter { it.amount > 0 }
                    .sortedByDescending { it.amount })

                Text(
                    stringResource(R.string.my_balances).toUpperCase(Locale.current),
                    fontSize = headerFontSize,
                    color = Color.DarkGray)

                // Balance List
                LazyRow(
                    contentPadding = PaddingValues(top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    items(items = balanceRatesSnapShot, itemContent = {rate: Rate ->
                        Text("${rate.amount} ${rate.currency}",
                            fontSize = 18.sp, color = Color.Black)
                    })
                }

                Text(
                    stringResource(R.string.app_name).toUpperCase(Locale.current),
                    modifier = Modifier.padding(PaddingValues(top = 20.dp)),
                    fontSize = headerFontSize,
                    color = Color.DarkGray)

                SellRowView(it, onEvent)

                ReceiveRowView(it, onEvent)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onEvent(MainScreenEvent.OnUpdateBalance)
                        showDialog = true
                    },
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .padding(top = 60.dp, bottom = 40.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.primary_color),
                        contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.submit), fontSize = 18.sp)
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text(
                            stringResource(R.string.currency_converted),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight =
                                FontWeight.Medium),
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Text(dialogMessage,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight =
                                FontWeight.Medium),
                            fontSize = 16.sp
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                        }) {
                            Text("Done")
                        }
                    }
                )
            }

            if(state is MainScreenState.Loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProgressDialog()
                }
            }

            if(state is MainScreenState.Error) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues ?: PaddingValues(0.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.currency_converter_unavailable))

                    Toast.makeText(LocalContext.current,
                        state.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}

@Composable
fun SellRowView(info: ExchangeRateInfo, onEvent: (MainScreenEvent) -> Unit) {
    val sellRates = info.rates.filter { it.amount > 0 }
    val sellRowRate = info.sellRate ?: sellRates[0]
    val sellAmount = info.sellValue ?: sellRowRate.amount

    var text by remember { mutableStateOf(sellAmount.toString()) }
    text = sellAmount.toString()

    val customTextSelectionColors = TextSelectionColors(
        handleColor = colorResource(R.color.primary_color),
        backgroundColor = colorResource(R.color.primary_color),
    )

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(PaddingValues(top = 20.dp))) {

        LeftRow(R.drawable.arrow_circle_up, R.string.sell)

        Spacer(modifier = Modifier.weight(1f))

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            TextField(
                value = text,
                onValueChange =
                    { newText ->
                        val num = newText.toFloatOrNull()
                        num?.let {
                            val filteredText = newText.filter { it.isDigit() || it == '.' }
                            if (filteredText.count { it == '.' } <= 1) {
                                val parts = filteredText.split('.')
                                var isFiltered = false
                                if (parts.size > 1) {
                                    val decimalPart = parts[1]
                                    if (decimalPart.length <= 2) {
                                        isFiltered = true
                                    }
                                } else {
                                    isFiltered = true
                                }

                                if (it <= sellRowRate.amount && it >= 0 && isFiltered) {
                                    text = newText
                                    onEvent(MainScreenEvent.OnUpdateSellValue(newText.toFloat()))
                                }
                            }
                        } ?: run {
                            text = newText
                            onEvent(MainScreenEvent.OnUpdateSellValue(0f))
                        }
                    },
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(40.dp, 120.dp)
                    .padding(start = 20.dp, end = 5.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = colorResource(R.color.primary_color),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = colorResource(R.color.primary_color),
                    cursorColor = Color.Black,
                ),
                textStyle = TextStyle(textAlign = TextAlign.End),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }

        DropDownMenu(sellRates, sellRowRate, onEvent)
    }

    HorizontalDivider(color = Color.LightGray, modifier =
        Modifier.padding(top = 10.dp, start = 65.dp))
}

@Composable
fun ReceiveRowView(info: ExchangeRateInfo,
                   onEvent: (MainScreenEvent) -> Unit) {
    val sellRates = info.rates.filter { it.amount > 0 }
    val sellRowRate = info.sellRate ?: sellRates[0]
    val sellAmount = info.sellValue ?: sellRowRate.amount

    val receiveRates =
        if(info.sellRate == null) info.rates.filter { it.conversionMap.contains(sellRates[0].currency) }
        else info.rates.filter { it.conversionMap.contains(info.sellRate.currency) }

    val receiveRowRate = info.receiveRate ?: receiveRates[0]
    val receiveAmount = info.receiveValue ?: (receiveRowRate.conversionMap[sellRowRate.currency]?.value?.let {
        round(it * sellAmount * 100) / 100
    } ?: 0f)

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(PaddingValues(top = 20.dp))) {
        LeftRow(R.drawable.arrow_circle_down, R.string.receive)

        Spacer(modifier = Modifier.weight(1f))

        Text(receiveAmount.toString(), modifier = Modifier
            .wrapContentWidth()
            .widthIn(40.dp, 120.dp)
            .padding(start = 20.dp, end = 10.dp),
            color = colorResource(R.color.green))

        DropDownMenu(receiveRates, receiveRowRate, onEvent, false)
    }
}

@Composable
fun DropDownMenu(rates: List<Rate>, selectedRate: Rate, onEvent: (MainScreenEvent) -> Unit,
                 isSell: Boolean = true) {

    val currencies = mutableListOf<String>()
    rates.forEach {
        currencies.add(it.currency)
    }

    val currency = currencies.find { it == selectedRate.currency }.orEmpty()

    var selectedCurrency by remember { mutableStateOf<String?>(currency) }
    selectedCurrency = currency

    LargeSearchableDropdownMenu(
        options = currencies,
        selectedOption = selectedCurrency,
        onItemSelected = {
            selectedCurrency = it
            rates.find { it.currency == selectedCurrency }?.let { rate ->
                if(isSell)
                    onEvent(MainScreenEvent.OnUpdateSellCurrency(rate))
                else
                    onEvent(MainScreenEvent.OnUpdateReceiveCurrency(rate))
            }
        },
        placeholder = "Currency",
        drawItem = { item, _, _, onClick ->
            DropdownMenuItem(onClick = onClick,
                text = item,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                selected = selectedCurrency == item,
                enabled = true)
        },
        modifier = Modifier
            .width(100.dp)
            .background(Color.White)
    )
}

@Composable
fun LeftRow(@DrawableRes imageId: Int, @StringRes strId: Int) {
    val iconTextFontSize = 18.sp
    val imageSize = Modifier.size(width = 50.dp, height = 50.dp)

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentSize()) {
        Image(
            modifier = imageSize,
            painter = painterResource(id = imageId),
            contentDescription = stringResource(strId),
        )

        Text(
            stringResource(strId),
            modifier = Modifier.padding(PaddingValues(start = 5.dp)),
            fontSize = iconTextFontSize,
            color = Color.DarkGray
        )
    }
}