package com.application.currency.exchange.presentation.view.content

import android.R.attr.text
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.application.currency.exchange.domain.entity.model.Rate
import com.application.currency.exchange.presentation.event.MainScreenEvent
import com.application.currency.exchange.presentation.state.MainScreenState
import com.application.currency.exchange.presentation.view.custom.ProgressDialog
import com.kanyidev.searchable_dropdown.DropdownMenuItem
import com.kanyidev.searchable_dropdown.LargeSearchableDropdownMenu
import kotlin.collections.filter

@Composable
fun MainScreenContent(state: MainScreenState, onEvent: (MainScreenEvent) -> Unit,
                      paddingValues: PaddingValues? = null) {
    when(state) {
        is MainScreenState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProgressDialog()
            }
        }
        is MainScreenState.Success -> {
            val rates = state.list
            CurrencyConverterContent(paddingValues, rates, onEvent)
        }

        // Shows that currency converter is unavailable if there is an error in retrieval
        // e.g. No Internet Connection
        is MainScreenState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues ?: PaddingValues(0.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.currency_converter_unavailable))

                Toast.makeText(LocalContext.current,
                    state.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        is MainScreenState.ReceiveValueUpdated -> {
            CurrencyConverterContent(paddingValues, state.list,
                onEvent, state.sellRate, state.receiveRate,
                state.sellValue, state.receiveValue)
        }

        else -> {

        }
    }
}

@Composable
fun CurrencyConverterContent(paddingValues: PaddingValues?, rates: List<Rate>,
                             onEvent: (MainScreenEvent) -> Unit,
                             sellRate: Rate? = null, receiveRate: Rate? = null,
                             sellValue: Float? = null, receiveValue: Float? = null) {
    val headerFontSize = 20.sp

    val sellRates = rates.filter { it.baseCurrency == null }
    val sellRowRate = sellRate ?: sellRates[0]
    val sellAmount = sellValue ?: sellRowRate.amount
    val receiveRowRate = receiveRate ?: rates[0]
    val receiveAmount = receiveValue ?: (receiveRowRate.convertedValue * sellAmount)

    onEvent(MainScreenEvent.OnInitSellValue(sellAmount))
    onEvent(MainScreenEvent.OnInitReceiveValue(receiveAmount))
    onEvent(MainScreenEvent.OnInitSellCurrency(sellRowRate))
    onEvent(MainScreenEvent.OnInitReceiveCurrency(receiveRowRate))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues ?: PaddingValues(0.dp))
            .padding(PaddingValues(20.dp))
            .imePadding()
    ) {
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
            items(items = rates.sortedByDescending { it.amount }, itemContent = {rate: Rate ->
                Text("${rate.amount} ${rate.currency}",
                    fontSize = 18.sp, color = Color.Black)
            })
        }

        Text(
            stringResource(R.string.app_name).toUpperCase(Locale.current),
            modifier = Modifier.padding(PaddingValues(top = 20.dp)),
            fontSize = headerFontSize,
            color = Color.DarkGray)

        SellRowView(sellRates, sellRowRate, sellAmount.toString(), onEvent)

        ReceiveRowView(rates, receiveRowRate, receiveAmount.toString(),
            onEvent)

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { onEvent(MainScreenEvent.OnUpdateBalance)},
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
}

@Composable
fun SellRowView(rates: List<Rate>, selectedRate: Rate, amount: String,
                onEvent: (MainScreenEvent) -> Unit) {
    var text by remember { mutableStateOf(amount) }

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
                onValueChange = { newText -> text = newText },
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(40.dp, 120.dp)
                    .padding(start = 20.dp, end = 5.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = colorResource(R.color.primary_color),
                    disabledTextColor = colorResource(R.color.green),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = colorResource(R.color.primary_color),
                    cursorColor = Color.Black,
                ),
                textStyle = TextStyle(textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }

        DropDownMenu(rates, selectedRate, onEvent)
    }

    HorizontalDivider(color = Color.LightGray, modifier =
        Modifier.padding(top = 10.dp, start = 65.dp))
}

@Composable
fun ReceiveRowView(rates: List<Rate>, selectedRate: Rate, amount: String,
                   onEvent: (MainScreenEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(PaddingValues(top = 20.dp))) {
        LeftRow(R.drawable.arrow_circle_down, R.string.receive)

        Spacer(modifier = Modifier.weight(1f))

        Text(amount, modifier = Modifier.wrapContentWidth()
            .widthIn(40.dp, 120.dp)
            .padding(start = 20.dp, end = 10.dp),
            color = colorResource(R.color.green))

        DropDownMenu(rates, selectedRate, onEvent, false)
    }
}

@Composable
fun DropDownMenu(rates: List<Rate>, selectedRate: Rate, onEvent: (MainScreenEvent) -> Unit,
                 isSell: Boolean = true) {

    val currencies = mutableListOf<String>()
    rates.forEach {
        currencies.add(it.currency)
    }

    val selectedCurrency = currencies.find { it == selectedRate.currency }.orEmpty()

    var selectedIndex by remember { mutableStateOf<String?>(selectedCurrency) }

    LargeSearchableDropdownMenu(
        options = currencies,
        selectedOption = selectedIndex,
        onItemSelected = {
            selectedIndex = it
            if(!isSell) {
                rates.find { it.currency == selectedIndex }?.let { rate ->
                    onEvent(MainScreenEvent.OnUpdateReceiveCurrency(rate))
                }
            }
        },
        placeholder = "Currency",
        drawItem = { item, _, _, onClick ->
            DropdownMenuItem(onClick = onClick,
                text = item,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                selected = selectedIndex == item,
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