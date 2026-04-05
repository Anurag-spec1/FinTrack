package com.hustlers.fintrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hustlers.fintrack.R
import com.hustlers.fintrack.utils.CurrencyManager

class CurrencyAdapter(
    private val currencies: List<CurrencyManager.Currency>,
    private val onCurrencySelected: (CurrencyManager.Currency) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        holder.bind(currency, onCurrencySelected)
    }

    override fun getItemCount() = currencies.size

    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFlag: TextView = itemView.findViewById(R.id.tvCurrencyFlag)
        private val tvCode: TextView = itemView.findViewById(R.id.tvCurrencyCode)
        private val tvName: TextView = itemView.findViewById(R.id.tvCurrencyName)
        private val tvSymbol: TextView = itemView.findViewById(R.id.tvCurrencySymbol)

        fun bind(currency: CurrencyManager.Currency, onCurrencySelected: (CurrencyManager.Currency) -> Unit) {
            tvFlag.text = currency.flag
            tvCode.text = currency.code
            tvName.text = currency.displayName
            tvSymbol.text = currency.symbol

            itemView.setOnClickListener {
                onCurrencySelected(currency)
            }
        }
    }
}