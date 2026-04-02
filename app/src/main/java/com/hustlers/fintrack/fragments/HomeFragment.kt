package com.hustlers.fintrack.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hustlers.fintrack.R
import com.hustlers.fintrack.databinding.FragmentHomeBinding
import com.hustlers.fintrack.databinding.ItemTransactionBinding
import com.hustlers.fintrack.dataclass.Transaction
import java.text.NumberFormat
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("hi", "IN")).apply {
        maximumFractionDigits = 0
    }
    private val balance    = 25_000.0
    private val income     = 15_000.0
    private val expenses   =  9_000.0
    private val savings    =  6_000.0
    private val goalTarget = 10_000.0
    private val goalProgress get() = ((savings / goalTarget) * 100).toInt()

    private val recentTransactions = listOf(
        Transaction("Zomato Order", "Food", -340.0, "🍔", "Today, 1:30 PM"),
        Transaction("Salary Credit",   "Income",  15000.0,   "💰", "Today, 10:00 AM"),
        Transaction("Petrol",          "Transport", -800.0,  "⛽", "Yesterday"),
        Transaction("Netflix",         "Bills",     -199.0,  "📺", "28 Jun"),
        Transaction("Grocery Store",   "Shopping",  -620.0,  "🛍", "27 Jun")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()
        animateEntrance()
        setupClickListeners()
    }

    private fun bindData() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else       -> "Good evening,"
        }

        binding.tvBalance.text         = formatRupee(balance)
        binding.tvBalanceIncome.text   = formatRupee(income)
        binding.tvBalanceExpenses.text = formatRupee(expenses)

        binding.tvIncome.text   = formatRupee(income)
        binding.tvExpenses.text = formatRupee(expenses)
        binding.tvSavings.text  = formatRupee(savings)

        binding.tvGoalAmount.text = "${formatRupee(savings)} of ${formatRupee(goalTarget)}"
        binding.progressGoal.progress = goalProgress

        val remaining = goalTarget - savings

        // Get the transaction binding objects
        val transactionBindings = listOf(
            binding.txn1,
            binding.txn2,
            binding.txn3,
            binding.txn4,
            binding.txn5
        )

        transactionBindings.forEachIndexed { i, txnBinding ->
            val txn = recentTransactions.getOrNull(i) ?: return@forEachIndexed
            bindTransaction(txnBinding, txn)
        }
    }

    private fun bindTransaction(txnBinding: ItemTransactionBinding, txn: Transaction) {
        txnBinding.txnIcon.text     = txn.icon
        txnBinding.txnTitle.text    = txn.title
        txnBinding.txnCategory.text = txn.category
        txnBinding.txnDate.text     = txn.date

        if (txn.amount >= 0) {
            txnBinding.txnAmount.text      = "+ ${formatRupee(txn.amount)}"
            txnBinding.txnAmount.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            txnBinding.txnAmount.text      = "- ${formatRupee(-txn.amount)}"
            txnBinding.txnAmount.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
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

            val fade     = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            val slide    = ObjectAnimator.ofFloat(view, "translationY", 40f, 0f)

            AnimatorSet().apply {
                playTogether(fade, slide)
                duration     = 400
                startDelay   = (index * 80).toLong()
                interpolator = DecelerateInterpolator(1.5f)
                start()
            }
        }

        animateCountUp(binding.tvBalance, 0.0, balance)
    }

    private fun animateCountUp(tv: TextView, from: Double, to: Double) {
        val animator = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
        animator.duration = 900
        animator.interpolator = DecelerateInterpolator(2f)
        animator.addUpdateListener { anim ->
            val v = anim.animatedValue as Float
            tv.text = formatRupee(v.toDouble())
        }
        animator.start()
    }

    private fun setupClickListeners() {
        binding.cardAddTransaction.setOnClickListener {
            // Navigate to Add fragment
            // findNavController().navigate(R.id.action_home_to_add)
        }

        binding.btnSeeAll.setOnClickListener {
            // Navigate to Transactions fragment
            // findNavController().navigate(R.id.action_home_to_transactions)
        }

        binding.btnViewGoals.setOnClickListener {
            // Navigate to Goals fragment
            // findNavController().navigate(R.id.action_home_to_goals)
        }

        binding.btnViewInsights.setOnClickListener {
            // Navigate to Insights fragment
            // findNavController().navigate(R.id.action_home_to_insights)
        }

        binding.cardGoal.setOnClickListener {

        }

        binding.cardIncome.setOnClickListener {

        }

        binding.cardExpenses.setOnClickListener {

        }

        binding.cardSavings.setOnClickListener {

        }
    }

    private fun formatRupee(amount: Double): String {
        return "₹${"%,.0f".format(amount)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}