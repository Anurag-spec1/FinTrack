package com.hustlers.fintrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hustlers.fintrack.R
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.utils.CurrencyManager

class TransactionListAdapter(
    private val items: MutableList<Transaction>,
    private val currencyManager: CurrencyManager
) : RecyclerView.Adapter<TransactionListAdapter.TxnViewHolder>() {

    inner class TxnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View        = itemView.findViewById(R.id.cardTxn)
        val icon: TextView    = itemView.findViewById(R.id.txnIcon)
        val title: TextView   = itemView.findViewById(R.id.txnTitle)
        val category: TextView= itemView.findViewById(R.id.txnCategory)
        val date: TextView    = itemView.findViewById(R.id.txnDate)
        val amount: TextView  = itemView.findViewById(R.id.txnAmount)
        val badge: TextView   = itemView.findViewById(R.id.txnBadge)
        val divider: View     = itemView.findViewById(R.id.txnDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TxnViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction_full, parent, false)
        )

    override fun onBindViewHolder(holder: TxnViewHolder, position: Int) {
        val txn = items[position]

        holder.icon.text     = txn.icon
        holder.title.text    = txn.title
        holder.category.text = txn.category
        holder.date.text     = txn.date

        holder.divider.visibility = if (position < items.size - 1) View.VISIBLE else View.GONE

        holder.root.alpha = 0f
        holder.root.translationY = 20f
        holder.root.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(250)
            .setStartDelay((position * 40L).coerceAtMost(400L))
            .start()

        if (txn.amount >= 0) {
            holder.amount.text = "+ ${formatAmount(txn.amount)}"
            holder.amount.setTextColor(0xFF4ADE80.toInt())
            holder.badge.text = "INCOME"
            holder.badge.setBackgroundResource(R.drawable.badge_income_bg)
            holder.badge.setTextColor(0xFF4ADE80.toInt())
            holder.icon.setBackgroundResource(R.drawable.icon_bg_green)
        } else {
            holder.amount.text = "- ${formatAmount(-txn.amount)}"
            holder.amount.setTextColor(0xFFF87171.toInt())
            holder.badge.text = "EXPENSE"
            holder.badge.setBackgroundResource(R.drawable.badge_expense_bg)
            holder.badge.setTextColor(0xFFF87171.toInt())
            holder.icon.setBackgroundResource(R.drawable.icon_bg_red)
        }
    }

    override fun getItemCount() = items.size

    fun getItem(pos: Int): Transaction = items[pos]

    fun removeAt(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
        if (pos > 0) notifyItemChanged(pos - 1)
    }

    fun updateList(list: List<Transaction>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    private fun formatAmount(amount: Double): String {
        return currencyManager.formatAmount(amount)
    }
}