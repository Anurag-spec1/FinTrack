package com.hustlers.fintrack.activities

import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hustlers.fintrack.R
import com.hustlers.fintrack.adapter.CurrencyAdapter
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.utils.CurrencyManager

class CurrencySettingsActivity : AppCompatActivity() {

    private lateinit var currencyManager: CurrencyManager
    private lateinit var prefs: FinTrackPreferences
    private lateinit var adapter: CurrencyAdapter
    private lateinit var rvCurrencies: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvCurrentFlag: TextView
    private lateinit var tvCurrentCode: TextView
    private lateinit var tvCurrentName: TextView
    private lateinit var tvCurrentSymbol: TextView
    private lateinit var btnBack: TextView

    private var allCurrencies = listOf<CurrencyManager.Currency>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_settings)

        currencyManager = CurrencyManager(this)
        prefs = FinTrackPreferences.getInstance(this)

        bindViews()
        setupBackButton()
        setupRecyclerView()
        loadCurrentCurrency()
        setupSearch()
    }

    private fun bindViews() {
        rvCurrencies = findViewById(R.id.rvCurrencies)
        searchView = findViewById(R.id.searchCurrencies)
        tvCurrentFlag = findViewById(R.id.tvCurrentCurrencyFlag)
        tvCurrentCode = findViewById(R.id.tvCurrentCurrencyCode)
        tvCurrentName = findViewById(R.id.tvCurrentCurrencyName)
        tvCurrentSymbol = findViewById(R.id.tvCurrentCurrencySymbol)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentCurrency() {
        val current = currencyManager.currentCurrency
        tvCurrentFlag.text = current.flag
        tvCurrentCode.text = current.code
        tvCurrentName.text = current.displayName
        tvCurrentSymbol.text = current.symbol
    }

    private fun setupRecyclerView() {
        allCurrencies = currencyManager.getAllCurrencies()
        adapter = CurrencyAdapter(allCurrencies) { selectedCurrency ->
            changeCurrency(selectedCurrency)
        }
        rvCurrencies.layoutManager = LinearLayoutManager(this)
        rvCurrencies.adapter = adapter
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCurrencies(newText ?: "")
                return true
            }
        })
    }

    private fun filterCurrencies(query: String) {
        val filtered = if (query.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter {
                it.code.contains(query, ignoreCase = true) ||
                        it.displayName.contains(query, ignoreCase = true)  // Changed from .name to .displayName
            }
        }
        adapter = CurrencyAdapter(filtered) { selectedCurrency ->
            changeCurrency(selectedCurrency)
        }
        rvCurrencies.adapter = adapter
    }

    private fun changeCurrency(newCurrency: CurrencyManager.Currency) {
        val oldCurrency = currencyManager.currentCurrency

        if (oldCurrency.code != newCurrency.code) {
            AlertDialog.Builder(this)
                .setTitle("Change Currency")
                .setMessage("Converting all amounts from ${oldCurrency.symbol}${oldCurrency.code} to ${newCurrency.symbol}${newCurrency.code}. Continue?")
                .setPositiveButton("Convert") { _, _ ->
                    performCurrencyConversion(newCurrency)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(this, "Already using ${newCurrency.code}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performCurrencyConversion(newCurrency: CurrencyManager.Currency) {

        prefs.convertAllTransactionsToCurrency(newCurrency.code, currencyManager)

        currencyManager.currentCurrency = newCurrency

        loadCurrentCurrency()

        Toast.makeText(
            this,
            "Currency changed to ${newCurrency.symbol} ${newCurrency.code}",
            Toast.LENGTH_LONG
        ).show()

        setResult(RESULT_OK)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}