package com.hustlers.fintrack.fragments

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.hustlers.fintrack.R
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.views.BarChartView
import com.hustlers.fintrack.views.DonutChartView

class InsightsFragment : Fragment() {

    private lateinit var prefs: FinTrackPreferences

    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvNetSavings: TextView
    private lateinit var tvSavingsRate: TextView

    private lateinit var donutChartView: DonutChartView
    private lateinit var legendContainer: LinearLayout

    private lateinit var barChartView: BarChartView

    private lateinit var categoryBreakdownContainer: LinearLayout

    private lateinit var topSpendingContainer: LinearLayout

    private lateinit var chipWeek: TextView
    private lateinit var chipMonth: TextView
    private lateinit var chipAll: TextView

    private var allTransactions = listOf<Transaction>()
    private var filteredTransactions = listOf<Transaction>()
    private var currentPeriod = Period.MONTH

    enum class Period { WEEK, MONTH, ALL }

    private val categoryColors = listOf(
        0xFFF87171.toInt(),   // red
        0xFF4ADE80.toInt(),   // green
        0xFF60A5FA.toInt(),   // blue
        0xFFFBBF24.toInt(),   // yellow
        0xFFA78BFA.toInt(),   // purple
        0xFF34D399.toInt(),   // teal
        0xFFFB923C.toInt(),   // orange
        0xFFE879F9.toInt()    // pink
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_insights, container, false)
        prefs = FinTrackPreferences.getInstance(requireContext())
        bindViews(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPeriodChips()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun bindViews(root: View) {
        tvTotalIncome = root.findViewById(R.id.tvInsightIncome)
        tvTotalExpenses = root.findViewById(R.id.tvInsightExpenses)
        tvNetSavings = root.findViewById(R.id.tvInsightSavings)
        tvSavingsRate = root.findViewById(R.id.tvSavingsRate)
        donutChartView = root.findViewById(R.id.donutChartView)
        legendContainer = root.findViewById(R.id.legendContainer)
        barChartView = root.findViewById(R.id.barChartView)
        categoryBreakdownContainer = root.findViewById(R.id.categoryBreakdownContainer)
        topSpendingContainer = root.findViewById(R.id.topSpendingContainer)
        chipWeek = root.findViewById(R.id.chipWeek)
        chipMonth = root.findViewById(R.id.chipMonth)
        chipAll = root.findViewById(R.id.chipAllTime)
    }

    private fun setupPeriodChips() {
        selectChip(chipMonth)
        chipWeek.setOnClickListener {
            currentPeriod = Period.WEEK; selectChip(chipWeek); applyPeriodFilter()
        }
        chipMonth.setOnClickListener {
            currentPeriod = Period.MONTH; selectChip(chipMonth); applyPeriodFilter()
        }
        chipAll.setOnClickListener {
            currentPeriod = Period.ALL; selectChip(chipAll); applyPeriodFilter()
        }
    }

    private fun selectChip(selected: TextView) {
        listOf(chipWeek, chipMonth, chipAll).forEach {
            it.setBackgroundResource(R.drawable.chip_unselected_bg)
            it.setTextColor(0xCCFFFFFF.toInt())
        }
        selected.setBackgroundResource(R.drawable.chip_selected_blue_bg)
        selected.setTextColor(0xFF0A0A0A.toInt())
    }

    private fun loadData() {
        allTransactions = prefs.getTransactions()
        applyPeriodFilter()
    }

    private fun applyPeriodFilter() {
        val now = System.currentTimeMillis()
        filteredTransactions = when (currentPeriod) {
            Period.WEEK -> allTransactions.filter { isWithinDays(it.date, 7) }
            Period.MONTH -> allTransactions.filter { isWithinDays(it.date, 30) }
            Period.ALL -> allTransactions
        }
        renderAll()
    }

    private fun isWithinDays(dateStr: String, days: Int): Boolean {
        return try {
            val sdfWithYear = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            val sdfOld      = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())

            val date = try {
                sdfWithYear.parse(dateStr)
            } catch (e: Exception) {
                sdfOld.parse(dateStr)
            } ?: return true

            val diff = System.currentTimeMillis() - date.time
            diff in 0..(days * 24 * 60 * 60 * 1000L)
        } catch (e: Exception) {
            true
        }
    }

    private fun renderAll() {
        renderSummaryCards()
        renderDonutChart()
        renderBarChart()
        renderCategoryBreakdown()
        renderTopSpending()
    }

    private fun renderSummaryCards() {
        val income = filteredTransactions.filter { it.amount >= 0 }.sumOf { it.amount }
        val expenses = filteredTransactions.filter { it.amount < 0 }.sumOf { -it.amount }
        val savings = income - expenses
        val rate = if (income > 0) ((savings / income) * 100).toInt() else 0

        tvTotalIncome.text = formatRupee(income)
        tvTotalExpenses.text = formatRupee(expenses)
        tvNetSavings.text = formatRupee(savings)
        tvSavingsRate.text = "$rate%"
        tvNetSavings.setTextColor(if (savings >= 0) 0xFF4ADE80.toInt() else 0xFFF87171.toInt())
    }

    private fun renderDonutChart() {
        val expenses = filteredTransactions.filter { it.amount < 0 }
        val grouped = expenses.groupBy { it.category }
            .map { (cat, txns) -> cat to txns.sumOf { -it.amount } }
            .sortedByDescending { it.second }
            .take(6)

        val total = grouped.sumOf { it.second }

        if (grouped.isEmpty()) {
            donutChartView.setData(emptyList())
            legendContainer.removeAllViews()
            return
        }

        val segments = grouped.mapIndexed { i, (_, amount) ->
            DonutChartView.Segment(
                sweep = ((amount / total) * 360f).toFloat(),
                color = categoryColors[i % categoryColors.size]
            )
        }
        donutChartView.setData(segments)

        legendContainer.removeAllViews()
        grouped.forEachIndexed { i, (cat, amount) ->
            val pct = ((amount / total) * 100).toInt()
            val row = buildLegendRow(
                cat,
                formatRupee(amount),
                "$pct%",
                categoryColors[i % categoryColors.size]
            )
            legendContainer.addView(row)
        }
    }

    private fun buildLegendRow(label: String, amount: String, pct: String, color: Int): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(8) }
        }

        val dot = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(10), dpToPx(10)).also {
                it.marginEnd = dpToPx(8)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
        }

        val tvLabel = TextView(requireContext()).apply {
            text = label
            textSize = 12f
            setTextColor(0xCCFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvPct = TextView(requireContext()).apply {
            text = pct
            textSize = 11f
            setTextColor(0x99FFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.marginEnd = dpToPx(8) }
        }

        val tvAmount = TextView(requireContext()).apply {
            text = amount
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(color)
        }

        row.addView(dot)
        row.addView(tvLabel)
        row.addView(tvPct)
        row.addView(tvAmount)
        return row
    }

    private fun renderBarChart() {
        val expenses = filteredTransactions.filter { it.amount < 0 }
        val income = filteredTransactions.filter { it.amount >= 0 }

        val expenseByCategory = expenses
            .groupBy { it.category }
            .map { (cat, txns) -> cat to txns.sumOf { -it.amount } }
            .sortedByDescending { it.second }
            .take(5)

        val incomeTotal = income.sumOf { it.amount }

        if (expenseByCategory.isEmpty()) {
            barChartView.setData(emptyList(), emptyList())
            return
        }

        val labels = expenseByCategory.map { it.first }
        val values = expenseByCategory.map { it.second.toFloat() }
        barChartView.setData(labels, values)
    }

    private fun renderCategoryBreakdown() {
        categoryBreakdownContainer.removeAllViews()

        val expenses = filteredTransactions.filter { it.amount < 0 }
        val grouped = expenses.groupBy { it.category }
            .map { (cat, txns) ->
                val icon = txns.firstOrNull()?.icon ?: "💳"
                Triple(icon, cat, txns.sumOf { -it.amount })
            }
            .sortedByDescending { it.third }
            .take(6)

        val maxAmount = grouped.maxOfOrNull { it.third } ?: 1.0

        grouped.forEachIndexed { i, (icon, cat, amount) ->
            val progress = ((amount / maxAmount) * 100).toInt()
            val color = categoryColors[i % categoryColors.size]
            val view = buildCategoryRow(icon, cat, amount, progress, color)
            categoryBreakdownContainer.addView(view)

            if (i < grouped.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                    ).also { it.topMargin = 0; it.bottomMargin = 0 }
                    setBackgroundColor(0x15FFFFFF)
                }
                categoryBreakdownContainer.addView(divider)
            }
        }

        if (grouped.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No expense data for this period"
                setTextColor(0x66FFFFFF)
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(48)
                )
            }
            categoryBreakdownContainer.addView(tv)
        }
    }

    private fun buildCategoryRow(
        icon: String,
        cat: String,
        amount: Double,
        progress: Int,
        color: Int
    ): View {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(18), dpToPx(12), dpToPx(18), dpToPx(12))
        }

        val row = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(8) }
        }

        val tvIcon = TextView(requireContext()).apply {
            text = "$icon $cat"
            textSize = 13f
            setTextColor(0xCCFFFFFF.toInt())
            id = View.generateViewId()
        }

        val tvAmount = TextView(requireContext()).apply {
            text = formatRupee(amount)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(color)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also { it.addRule(RelativeLayout.ALIGN_PARENT_END) }
        }

        row.addView(tvIcon)
        row.addView(tvAmount)

        val progressBar = android.widget.ProgressBar(
            requireContext(), null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            this.progress = progress
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(5)
            )
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x22FFFFFF)
            progressTintList = android.content.res.ColorStateList.valueOf(color)
        }

        container.addView(row)
        container.addView(progressBar)
        return container
    }

    private fun renderTopSpending() {
        topSpendingContainer.removeAllViews()

        val top = filteredTransactions
            .filter { it.amount < 0 }
            .sortedBy { it.amount }
            .take(5)

        top.forEachIndexed { i, txn ->
            val row = buildTopSpendingRow(i + 1, txn)
            topSpendingContainer.addView(row)

            if (i < top.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also {
                        it.marginStart = dpToPx(18)
                        it.marginEnd = dpToPx(18)
                    }
                    setBackgroundColor(0x15FFFFFF)
                }
                topSpendingContainer.addView(divider)
            }
        }

        if (top.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No spending data for this period"
                setTextColor(0x66FFFFFF)
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(48)
                )
            }
            topSpendingContainer.addView(tv)
        }
    }

    private fun buildTopSpendingRow(rank: Int, txn: Transaction): View {
        val row = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(18), dpToPx(12), dpToPx(18), dpToPx(12))
        }

        val tvRank = TextView(requireContext()).apply {
            text = "#$rank"
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0x66FFFFFF)
            id = View.generateViewId()
        }
        val rankParams =
            RelativeLayout.LayoutParams(dpToPx(28), RelativeLayout.LayoutParams.WRAP_CONTENT)
        rankParams.addRule(RelativeLayout.CENTER_VERTICAL)
        tvRank.layoutParams = rankParams

        val tvIcon = TextView(requireContext()).apply {
            text = txn.icon
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            id = View.generateViewId()
        }

        val iconParams = RelativeLayout.LayoutParams(dpToPx(36), dpToPx(36))
        iconParams.addRule(RelativeLayout.END_OF, tvRank.id)
        iconParams.addRule(RelativeLayout.CENTER_VERTICAL)
        iconParams.marginStart = dpToPx(4)
        tvIcon.gravity = android.view.Gravity.CENTER
        tvIcon.layoutParams = iconParams

        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            id = View.generateViewId()
        }
        val infoParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        infoParams.addRule(RelativeLayout.END_OF, tvIcon.id)
        infoParams.addRule(RelativeLayout.CENTER_VERTICAL)
        infoParams.marginStart = dpToPx(10)
        infoLayout.layoutParams = infoParams

        val tvTitle = TextView(requireContext()).apply {
            text = txn.title
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
        }
        val tvCat = TextView(requireContext()).apply {
            text = txn.category
            textSize = 11f
            setTextColor(0x99FFFFFF.toInt())
        }
        infoLayout.addView(tvTitle)
        infoLayout.addView(tvCat)

        val tvAmount = TextView(requireContext()).apply {
            text = "- ${formatRupee(-txn.amount)}"
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFFF87171.toInt())
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.addRule(RelativeLayout.ALIGN_PARENT_END)
                it.addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        row.addView(tvRank)
        row.addView(tvIcon)
        row.addView(infoLayout)
        row.addView(tvAmount)
        return row
    }

    private val Int.sp get() = this.toFloat()

    private fun formatRupee(amount: Double) = "₹${"%,.0f".format(amount)}"

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}