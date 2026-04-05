package com.hustlers.fintrack.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.hustlers.fintrack.MainActivity
import com.hustlers.fintrack.R
import com.hustlers.fintrack.activities.AddTransactionActivity
import com.hustlers.fintrack.activities.ProfileActivity
import com.hustlers.fintrack.activities.SettingsActivity
import com.hustlers.fintrack.databinding.FragmentHomeBinding
import com.hustlers.fintrack.databinding.ItemTransactionBinding
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.utils.CurrencyManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: FinTrackPreferences
    private lateinit var currencyManager: CurrencyManager
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var cardProfile: CardView

    private val categoryColors = listOf(
        0xFFF87171.toInt(),
        0xFF4ADE80.toInt(),
        0xFF60A5FA.toInt(),
        0xFFFBBF24.toInt(),
        0xFFA78BFA.toInt(),
        0xFF34D399.toInt(),
        0xFFFB923C.toInt(),
        0xFFE879F9.toInt()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = FinTrackPreferences.getInstance(requireContext())
        currencyManager = CurrencyManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivProfilePhoto = binding.ivProfilePhoto
        cardProfile = binding.cardProfile
        bindData()
        animateEntrance()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }

    private fun bindData() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
        binding.tvUserName.text = prefs.userName

        loadProfilePhoto()

        binding.tvBalance.text         = formatAmount(prefs.balance)
        binding.tvBalanceIncome.text   = formatAmount(prefs.totalIncome)
        binding.tvBalanceExpenses.text = formatAmount(prefs.totalExpenses)

        binding.tvIncome.text   = formatAmount(prefs.totalIncome)
        binding.tvExpenses.text = formatAmount(prefs.totalExpenses)
        binding.tvSavings.text  = formatAmount(prefs.totalSavings)

        binding.tvGoalAmount.text    = formatAmount(prefs.totalSavings)
        binding.tvMonthlyTarget.text = "of ${formatAmount(prefs.goalTarget)}"
        binding.progressGoal.progress = prefs.goalProgress

        val transactionBindings = listOf(
            binding.txn1, binding.txn2, binding.txn3, binding.txn4, binding.txn5
        )
        val recent = prefs.getRecentTransactions(5)

        transactionBindings.forEachIndexed { i, txnBinding ->
            val txn = recent.getOrNull(i)
            if (txn != null) {
                txnBinding.root.visibility = View.VISIBLE
                bindTransaction(txnBinding, txn)
            } else {
                txnBinding.root.visibility = View.GONE
            }
        }

        loadSpendingByCategory()
    }

    private fun loadProfilePhoto() {
        val photoBitmap = prefs.getUserPhotoBitmap()
        if (photoBitmap != null) {
            ivProfilePhoto.setImageBitmap(photoBitmap)
        } else {
            ivProfilePhoto.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun bindTransaction(txnBinding: ItemTransactionBinding, txn: Transaction) {
        txnBinding.txnIcon.text     = txn.icon
        txnBinding.txnTitle.text    = txn.title
        txnBinding.txnCategory.text = txn.category
        txnBinding.txnDate.text     = txn.date

        if (txn.amount >= 0) {
            txnBinding.txnAmount.text = "+ ${formatAmount(txn.amount)}"
            txnBinding.txnAmount.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            txnBinding.txnAmount.text = "- ${formatAmount(-txn.amount)}"
            txnBinding.txnAmount.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
        }
    }

    private fun loadSpendingByCategory() {
        val allTransactions = prefs.getTransactions()

        val recentExpenses = allTransactions.filter { transaction ->
            transaction.amount < 0 && isWithinDays(transaction.date, 30)
        }

        val categorySpending = recentExpenses
            .groupBy { it.category }
            .map { (category, transactions) ->
                val total = transactions.sumOf { -it.amount }
                val icon = transactions.firstOrNull()?.icon ?: getDefaultIconForCategory(category)
                Triple(category, total, icon)
            }
            .sortedByDescending { it.second }
            .take(5)

        binding.categorySpendingContainer.removeAllViews()

        if (categorySpending.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No spending data in last 30 days"
                textSize = 13f
                setTextColor(0x66FFFFFF.toInt())
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(80)
                )
            }
            binding.categorySpendingContainer.addView(emptyView)
            return
        }

        val totalSpent = categorySpending.sumOf { it.second }

        categorySpending.forEachIndexed { index, (name, amount, icon) ->
            val percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toInt() else 0
            val color = getColorForCategory(name, index)
            val categoryView = createCategorySpendingView(icon, name, amount, percentage, color)
            binding.categorySpendingContainer.addView(categoryView)

            if (index < categorySpending.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                    ).also {
                        it.topMargin = dpToPx(12)
                        it.bottomMargin = dpToPx(12)
                    }
                    setBackgroundColor(0x15FFFFFF)
                }
                binding.categorySpendingContainer.addView(divider)
            }
        }
    }

    private fun createCategorySpendingView(
        icon: String,
        name: String,
        amount: Double,
        percentage: Int,
        color: Int
    ): View {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val topRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(8) }
        }

        val iconView = TextView(requireContext()).apply {
            text = icon
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            val size = dpToPx(32)
            layoutParams = LinearLayout.LayoutParams(size, size).also {
                it.marginEnd = dpToPx(12)
            }
            background = createCircleBackground(color, 30)
        }

        val nameView = TextView(requireContext()).apply {
            text = name
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val amountView = TextView(requireContext()).apply {
            text = formatAmount(amount)
            textSize = 14f
            setTextColor(color)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(iconView)
        topRow.addView(nameView)
        topRow.addView(amountView)

        val progressRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = percentage
            layoutParams = LinearLayout.LayoutParams(
                0,
                dpToPx(4),
                1f
            ).also { it.marginEnd = dpToPx(12) }
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x22FFFFFF)
            progressTintList = android.content.res.ColorStateList.valueOf(color)
        }

        val percentageView = TextView(requireContext()).apply {
            text = "$percentage%"
            textSize = 11f
            setTextColor(0x99FFFFFF.toInt())
        }

        progressRow.addView(progressBar)
        progressRow.addView(percentageView)

        container.addView(topRow)
        container.addView(progressRow)

        return container
    }

    private fun createCircleBackground(color: Int, alpha: Int = 25): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            setColor(android.graphics.Color.argb(alpha, r, g, b))
        }
    }

    private fun getColorForCategory(category: String, index: Int): Int {
        return when (category.lowercase()) {
            "food" -> 0xFFF87171.toInt()
            "shopping" -> 0xFF4ADE80.toInt()
            "transport" -> 0xFF60A5FA.toInt()
            "bills" -> 0xFFFBBF24.toInt()
            "health" -> 0xFFA78BFA.toInt()
            "entertainment" -> 0xFF34D399.toInt()
            "rent" -> 0xFFFB923C.toInt()
            "education" -> 0xFFE879F9.toInt()
            else -> categoryColors[index % categoryColors.size]
        }
    }

    private fun getDefaultIconForCategory(category: String): String {
        return when (category.lowercase()) {
            "food" -> "🍔"
            "shopping" -> "🛍"
            "transport" -> "⛽"
            "bills" -> "💡"
            "health" -> "🏥"
            "entertainment" -> "🎮"
            "rent" -> "🏠"
            "education" -> "🎓"
            else -> "📦"
        }
    }

    private fun isWithinDays(dateStr: String, days: Int): Boolean {
        return try {
            val sdfWithYear = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            val sdfOld = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())

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

    private fun animateEntrance() {
        val views = listOf(
            binding.greetingRow,
            binding.cardBalance,
            binding.cardIncome,
            binding.cardExpenses,
            binding.cardSavings,
            binding.cardGoal
        )
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "translationY", 40f, 0f)
                )
                duration     = 400
                startDelay   = (index * 80).toLong()
                interpolator = DecelerateInterpolator(1.5f)
                start()
            }
        }
        animateCountUp(binding.tvBalance, 0.0, prefs.balance)
    }

    private fun animateCountUp(tv: TextView, from: Double, to: Double) {
        if (!isAdded || view == null) return
        ValueAnimator.ofFloat(from.toFloat(), to.toFloat()).apply {
            duration     = 900
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener {
                if (isAdded) {
                    tv.text = formatAmount((animatedValue as Float).toDouble())
                }
            }
            start()
        }
    }

    private fun setupClickListeners() {
        binding.cardAddTransaction.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), AddTransactionActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), SettingsActivity::class.java))
        }

        cardProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        binding.btnSeeAll.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.selectTab(1)
                when (1) {
                    0 -> mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    1 -> mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TransactionsFragment())
                        .commit()
                    2 -> mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, InsightsFragment())
                        .commit()
                    3 -> mainActivity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, GoalsFragment())
                        .commit()
                }
            }
        }

        binding.btnViewGoals.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.selectTab(3)
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, GoalsFragment())
                    .commit()
            }
        }

        binding.btnViewInsights.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.selectTab(2)
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, InsightsFragment())
                    .commit()
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        return currencyManager.formatAmount(amount)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}