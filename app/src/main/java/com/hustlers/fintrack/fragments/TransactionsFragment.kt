package com.hustlers.fintrack.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.hustlers.fintrack.R
import com.hustlers.fintrack.adapter.TransactionListAdapter
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.utils.CurrencyManager

class TransactionsFragment : Fragment() {

    private lateinit var prefs: FinTrackPreferences
    private lateinit var currencyManager: CurrencyManager

    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipIncome: Chip
    private lateinit var chipExpense: Chip
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var layoutEmpty: View
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvTotalSavings: TextView
    private lateinit var tvTransactionCount: TextView

    private lateinit var adapter: TransactionListAdapter

    private var allTransactions = listOf<Transaction>()
    private var currentFilter = Filter.ALL
    private var currentSearch = ""

    enum class Filter { ALL, INCOME, EXPENSE }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_transactions, container, false)
        prefs = FinTrackPreferences.getInstance(requireContext())
        currencyManager = CurrencyManager(requireContext())
        bindViews(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupChips()
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun bindViews(root: View) {
        etSearch = root.findViewById(R.id.etSearch)
        btnClearSearch = root.findViewById(R.id.btnClearSearch)
        chipGroup = root.findViewById(R.id.chipGroup)
        chipAll = root.findViewById(R.id.chipAll)
        chipIncome = root.findViewById(R.id.chipIncome)
        chipExpense = root.findViewById(R.id.chipExpense)
        rvTransactions = root.findViewById(R.id.rvTransactions)
        tvEmpty = root.findViewById(R.id.tvEmpty)
        tvEmptySubtitle = root.findViewById(R.id.tvEmptySubtitle)
        layoutEmpty = root.findViewById(R.id.layoutEmpty)
        tvTotalIncome = root.findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = root.findViewById(R.id.tvTotalExpenses)
        tvTotalSavings = root.findViewById(R.id.tvTotalSavings)
        tvTransactionCount = root.findViewById(R.id.tvTransactionCount)
    }

    private fun setupRecyclerView() {
        adapter = TransactionListAdapter(mutableListOf(), currencyManager)
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = adapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                val txn = adapter.getItem(position)

                prefs.deleteTransaction(txn.id)
                allTransactions = allTransactions.filter { it.id != txn.id }
                adapter.removeAt(position)
                updateSummaryCards()
                checkEmptyState()

                Snackbar.make(rvTransactions, "\"${txn.title}\" deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        prefs.addTransaction(txn)
                        loadTransactions()
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(rvTransactions)
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearch = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility =
                    if (currentSearch.isEmpty()) View.GONE else View.VISIBLE
                applyFilters()
            }
        })
        btnClearSearch.setOnClickListener {
            etSearch.setText("")
        }
    }

    private fun setupChips() {
        chipAll.setOnClickListener { currentFilter = Filter.ALL; applyFilters() }
        chipIncome.setOnClickListener { currentFilter = Filter.INCOME; applyFilters() }
        chipExpense.setOnClickListener { currentFilter = Filter.EXPENSE; applyFilters() }
    }

    private fun loadTransactions() {
        allTransactions = prefs.getTransactions()
        updateSummaryCards()
        applyFilters()
    }

    private fun applyFilters() {
        var result = allTransactions

        result = when (currentFilter) {
            Filter.ALL -> result
            Filter.INCOME -> result.filter { it.amount >= 0 }
            Filter.EXPENSE -> result.filter { it.amount < 0 }
        }

        if (currentSearch.isNotEmpty()) {
            result = result.filter {
                it.title.contains(currentSearch, ignoreCase = true) ||
                        it.category.contains(currentSearch, ignoreCase = true)
            }
        }

        adapter.updateList(result)
        tvTransactionCount.text =
            "${result.size} transaction${if (result.size != 1) "s" else ""}"
        checkEmptyState()
    }

    private fun updateSummaryCards() {
        val income = allTransactions.filter { it.amount >= 0 }.sumOf { it.amount }
        val expenses = allTransactions.filter { it.amount < 0 }.sumOf { -it.amount }
        tvTotalIncome.text = formatAmount(income)
        tvTotalExpenses.text = formatAmount(expenses)
        tvTotalSavings.text = formatAmount(income - expenses)
    }

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            layoutEmpty.visibility = View.VISIBLE
            rvTransactions.visibility = View.GONE
            tvEmpty.text = if (currentSearch.isNotEmpty()) "No results found" else "No transactions yet"
            tvEmptySubtitle.text = if (currentSearch.isNotEmpty()) "Try a different search term" else "Tap + to add your first transaction"
        } else {
            layoutEmpty.visibility = View.GONE
            rvTransactions.visibility = View.VISIBLE
        }
    }

    private fun formatAmount(amount: Double): String {
        return currencyManager.formatAmount(amount)
    }
}