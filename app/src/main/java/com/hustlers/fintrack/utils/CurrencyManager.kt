package com.hustlers.fintrack.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.*

class CurrencyManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    enum class Currency(val code: String, val symbol: String, val displayName: String, val flag: String) {
        USD("USD", "$", "US Dollar", "🇺🇸"),
        INR("INR", "₹", "Indian Rupee", "🇮🇳"),
        EUR("EUR", "€", "Euro", "🇪🇺"),
        GBP("GBP", "£", "British Pound", "🇬🇧"),
        JPY("JPY", "¥", "Japanese Yen", "🇯🇵"),
        CAD("CAD", "C$", "Canadian Dollar", "🇨🇦"),
        AUD("AUD", "A$", "Australian Dollar", "🇦🇺"),
        CNY("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        SGD("SGD", "S$", "Singapore Dollar", "🇸🇬"),
        AED("AED", "د.إ", "UAE Dirham", "🇦🇪"),
        KRW("KRW", "₩", "South Korean Won", "🇰🇷"),
        RUB("RUB", "₽", "Russian Ruble", "🇷🇺"),
        BRL("BRL", "R$", "Brazilian Real", "🇧🇷"),
        ZAR("ZAR", "R", "South African Rand", "🇿🇦")
    }

    var currentCurrency: Currency
        get() {
            val currencyCode = prefs.getString("selected_currency", "INR") ?: "INR"
            return Currency.values().find { it.code == currencyCode } ?: Currency.INR
        }
        set(value) {
            prefs.edit().putString("selected_currency", value.code).apply()
        }

    fun convertAmount(amount: Double, toCurrency: Currency): Double {
        val fromCurrency = currentCurrency
        if (fromCurrency == toCurrency) return amount
        val rate = getExchangeRate(fromCurrency, toCurrency)
        return amount * rate
    }

    fun getExchangeRate(from: Currency, to: Currency): Double {
        val usdRates = mapOf(
            "USD" to 1.0,
            "INR" to 83.0,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "JPY" to 148.0,
            "CAD" to 1.35,
            "AUD" to 1.52,
            "CNY" to 7.19,
            "SGD" to 1.34,
            "AED" to 3.67,
            "KRW" to 1330.0,
            "RUB" to 91.0,
            "BRL" to 4.95,
            "ZAR" to 18.8
        )

        val fromRate = usdRates[from.code] ?: 1.0
        val toRate = usdRates[to.code] ?: 1.0

        return toRate / fromRate
    }

    fun formatAmount(amount: Double, currency: Currency = currentCurrency): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            formatter.currency = java.util.Currency.getInstance(currency.code)
            val formatted = formatter.format(amount)
            formatted.replace(java.util.Currency.getInstance(currency.code).symbol, currency.symbol)
        } catch (e: Exception) {
            "${currency.symbol} ${String.format("%,.2f", amount)}"
        }
    }

    fun formatAmountCompact(amount: Double, currency: Currency = currentCurrency): String {
        return when {
            amount >= 1_000_000 -> "${currency.symbol} ${String.format("%.1f", amount / 1_000_000)}M"
            amount >= 1_000 -> "${currency.symbol} ${String.format("%.1f", amount / 1_000)}K"
            else -> formatAmount(amount, currency)
        }
    }

    fun getAllCurrencies(): List<Currency> {
        return Currency.values().toList()
    }

    fun showCurrencySelector(callback: (Currency) -> Unit) {
        val currencies = getAllCurrencies()
        val currencyNames = currencies.map { "${it.flag} ${it.symbol} ${it.code} - ${it.displayName}" }.toTypedArray()

        val builder = android.app.AlertDialog.Builder(context)
            .setTitle("Select Currency")
            .setItems(currencyNames) { _, which ->
                val selected = currencies[which]
                currentCurrency = selected
                callback(selected)
            }

        builder.show()
    }

    fun getSavedAmountInUserCurrency(savedAmount: Double, savedCurrencyCode: String): Double {
        val savedCurrency = Currency.values().find { it.code == savedCurrencyCode } ?: Currency.INR
        val current = currentCurrency

        if (savedCurrency == current) return savedAmount

        val rate = getExchangeRate(savedCurrency, current)
        return savedAmount * rate
    }
}