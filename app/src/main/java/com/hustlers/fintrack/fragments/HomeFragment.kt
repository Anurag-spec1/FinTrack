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
import com.hustlers.fintrack.databinding.FragmentHomeBinding
import com.hustlers.fintrack.databinding.ItemTransactionBinding
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.storage.FinTrackPreferences

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: FinTrackPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = FinTrackPreferences.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindData()
        animateEntrance()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }


    private fun bindData() {
        // Greeting
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
        binding.tvUserName.text = prefs.userName

        binding.tvBalance.text         = formatRupee(prefs.balance)
        binding.tvBalanceIncome.text   = formatRupee(prefs.totalIncome)
        binding.tvBalanceExpenses.text = formatRupee(prefs.totalExpenses)

        binding.tvIncome.text   = formatRupee(prefs.totalIncome)
        binding.tvExpenses.text = formatRupee(prefs.totalExpenses)
        binding.tvSavings.text  = formatRupee(prefs.totalSavings)

        binding.tvGoalAmount.text    = "${formatRupee(prefs.totalSavings)} of ${formatRupee(prefs.goalTarget)}"
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
    }

    private fun bindTransaction(txnBinding: ItemTransactionBinding, txn: Transaction) {
        txnBinding.txnIcon.text     = txn.icon
        txnBinding.txnTitle.text    = txn.title
        txnBinding.txnCategory.text = txn.category
        txnBinding.txnDate.text     = txn.date

        if (txn.amount >= 0) {
            txnBinding.txnAmount.text = "+ ${formatRupee(txn.amount)}"
            txnBinding.txnAmount.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            txnBinding.txnAmount.text = "- ${formatRupee(-txn.amount)}"
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
        ValueAnimator.ofFloat(from.toFloat(), to.toFloat()).apply {
            duration     = 900
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { tv.text = formatRupee((animatedValue as Float).toDouble()) }
            start()
        }
    }


    private fun setupClickListeners() {
        binding.cardAddTransaction.setOnClickListener {
            // findNavController().navigate(R.id.action_home_to_add)
        }
        binding.btnSeeAll.setOnClickListener {
            // findNavController().navigate(R.id.action_home_to_transactions)
        }
        binding.btnViewGoals.setOnClickListener {
            // findNavController().navigate(R.id.action_home_to_goals)
        }
        binding.btnViewInsights.setOnClickListener {
            // findNavController().navigate(R.id.action_home_to_insights)
        }
    }

    private fun formatRupee(amount: Double) = "₹${"%,.0f".format(amount)}"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}