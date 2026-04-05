package com.hustlers.fintrack.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.hustlers.fintrack.R
import com.hustlers.fintrack.dataclass.Goal
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.storage.GoalPreferences
import com.hustlers.fintrack.utils.CurrencyManager

class GoalsFragment : Fragment() {

    private lateinit var prefs: FinTrackPreferences
    private lateinit var goalPrefs: GoalPreferences
    private lateinit var currencyManager: CurrencyManager

    private lateinit var tvTotalGoals: TextView
    private lateinit var tvCompletedGoals: TextView
    private lateinit var tvTotalSaved: TextView

    private lateinit var tvMonthlySavings: TextView
    private lateinit var tvMonthlyTarget: TextView
    private lateinit var progressMonthly: ProgressBar
    private lateinit var tvMonthlyPct: TextView
    private lateinit var tvMonthlyRemaining: TextView
    private lateinit var btnEditMonthlyGoal: TextView
    private lateinit var goalsContainer: LinearLayout
    private lateinit var layoutEmptyGoals: LinearLayout

    private lateinit var btnAddGoal: CardView

    private val goalColors = listOf(
        0xFF60A5FA.toInt(),
        0xFF4ADE80.toInt(),
        0xFFFBBF24.toInt(),
        0xFFA78BFA.toInt(),
        0xFFF87171.toInt(),
        0xFF34D399.toInt(),
        0xFFFB923C.toInt(),
        0xFFE879F9.toInt()
    )

    private val goalEmojis = listOf("🎯","🏠","✈️","🚗","📱","💻","👨‍🎓","💍","🏋️","🎮","📚","🏖️")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_goals, container, false)
        prefs = FinTrackPreferences.getInstance(requireContext())
        goalPrefs = GoalPreferences.getInstance(requireContext())
        currencyManager = CurrencyManager(requireContext())
        bindViews(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadAll()
    }

    override fun onResume() {
        super.onResume()
        loadAll()
    }

    private fun bindViews(root: View) {
        tvTotalGoals = root.findViewById(R.id.tvTotalGoals)
        tvCompletedGoals = root.findViewById(R.id.tvCompletedGoals)
        tvTotalSaved = root.findViewById(R.id.tvTotalSaved)
        tvMonthlySavings = root.findViewById(R.id.tvMonthlySavings)
        tvMonthlyTarget = root.findViewById(R.id.tvMonthlyTarget)
        progressMonthly = root.findViewById(R.id.progressMonthly)
        tvMonthlyPct = root.findViewById(R.id.tvMonthlyPct)
        tvMonthlyRemaining = root.findViewById(R.id.tvMonthlyRemaining)
        btnEditMonthlyGoal = root.findViewById(R.id.btnEditMonthlyGoal)
        goalsContainer = root.findViewById(R.id.goalsContainer)
        layoutEmptyGoals = root.findViewById(R.id.layoutEmptyGoals)
        btnAddGoal = root.findViewById(R.id.btnAddGoal)
    }

    private fun setupClickListeners() {
        btnAddGoal.setOnClickListener { showAddGoalDialog() }
        btnEditMonthlyGoal.setOnClickListener { showEditMonthlyGoalDialog() }
    }

    private fun loadAll() {
        loadMonthlySavingsGoal()
        loadCustomGoals()
        loadSummaryHeader()
    }

    private fun loadMonthlySavingsGoal() {
        val saved = prefs.totalSavings
        val target = prefs.goalTarget
        val pct = if (target > 0) ((saved / target) * 100).toInt().coerceIn(0, 100) else 0
        val remaining = (target - saved).coerceAtLeast(0.0)

        tvMonthlySavings.text = formatAmount(saved.coerceAtLeast(0.0))
        tvMonthlyTarget.text = "of ${formatAmount(target)}"
        progressMonthly.progress = pct
        tvMonthlyPct.text = "$pct%"
        tvMonthlyRemaining.text = if (remaining > 0)
            "${formatAmount(remaining)} to go"
        else
            "🎉 Goal achieved!"

        val color = when {
            pct >= 100 -> 0xFF4ADE80.toInt()
            pct >= 60 -> 0xFF60A5FA.toInt()
            pct >= 30 -> 0xFFFBBF24.toInt()
            else -> 0xFFF87171.toInt()
        }
        progressMonthly.progressTintList = android.content.res.ColorStateList.valueOf(color)
        tvMonthlyPct.setTextColor(color)
    }

    private fun loadCustomGoals() {
        goalsContainer.removeAllViews()
        val goals = goalPrefs.getGoals()

        if (goals.isEmpty()) {
            layoutEmptyGoals.visibility = View.VISIBLE
            goalsContainer.visibility = View.GONE
            return
        }

        layoutEmptyGoals.visibility = View.GONE
        goalsContainer.visibility = View.VISIBLE

        goals.forEachIndexed { index, goal ->
            val color = goalColors[index % goalColors.size]
            val card = buildGoalCard(goal, color, index)
            goalsContainer.addView(card)
        }
    }

    private fun buildGoalCard(goal: Goal, color: Int, index: Int): View {
        val pct = if (goal.target > 0) ((goal.saved / goal.target) * 100).toInt().coerceIn(0, 100) else 0
        val remaining = (goal.target - goal.saved).coerceAtLeast(0.0)
        val isComplete = pct >= 100

        val card = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(12) }
            radius = dpToPx(20).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(0x00000000)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.glass_card_bg)
            setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16))
        }

        val row1 = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dpToPx(12) }
        }

        val tvEmoji = TextView(requireContext()).apply {
            text = goal.emoji
            textSize = 18f
            gravity = Gravity.CENTER
            id = View.generateViewId()
            background = buildCircleBg(color, 30)
        }
        val emojiParams = RelativeLayout.LayoutParams(dpToPx(40), dpToPx(40))
        emojiParams.addRule(RelativeLayout.CENTER_VERTICAL)
        tvEmoji.layoutParams = emojiParams

        val infoBlock = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            id = View.generateViewId()
        }
        val infoParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        infoParams.addRule(RelativeLayout.END_OF, tvEmoji.id)
        infoParams.addRule(RelativeLayout.CENTER_VERTICAL)
        infoParams.marginStart = dpToPx(12)
        infoBlock.layoutParams = infoParams

        val tvTitle = TextView(requireContext()).apply {
            text = goal.title
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }
        val tvSub = TextView(requireContext()).apply {
            text = if (isComplete) "🎉 Completed!" else "${formatAmount(goal.saved)} of ${formatAmount(goal.target)}"
            textSize = 11f
            setTextColor(if (isComplete) 0xFF4ADE80.toInt() else Color.parseColor("#99FFFFFF"))
        }
        infoBlock.addView(tvTitle)
        infoBlock.addView(tvSub)

        val actionsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.addRule(RelativeLayout.ALIGN_PARENT_END)
                it.addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        val btnAdd = TextView(requireContext()).apply {
            text = "＋"
            textSize = 18f
            setTextColor(color)
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            setOnClickListener { showAddMoneyDialog(goal) }
        }

        val btnDelete = TextView(requireContext()).apply {
            text = "🗑"
            textSize = 15f
            setPadding(dpToPx(12), dpToPx(8), dpToPx(8), dpToPx(8))
            setOnClickListener { showDeleteGoalDialog(goal) }
        }

        actionsLayout.addView(btnAdd)
        actionsLayout.addView(btnDelete)

        row1.addView(tvEmoji)
        row1.addView(infoBlock)
        row1.addView(actionsLayout)

        val progressBar = ProgressBar(
            requireContext(), null, android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = pct
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6)
            ).also { it.bottomMargin = dpToPx(8) }
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x22FFFFFF)
            progressTintList = android.content.res.ColorStateList.valueOf(
                if (isComplete) 0xFF4ADE80.toInt() else color
            )
        }

        val row3 = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvPct = TextView(requireContext()).apply {
            text = "$pct%"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (isComplete) 0xFF4ADE80.toInt() else color)
        }

        val tvRemaining = TextView(requireContext()).apply {
            text = if (isComplete) "Done ✓" else "${formatAmount(remaining)} remaining"
            textSize = 11f
            setTextColor(0x66FFFFFF)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).also { it.addRule(RelativeLayout.ALIGN_PARENT_END) }
        }

        row3.addView(tvPct)
        row3.addView(tvRemaining)

        container.addView(row1)
        container.addView(progressBar)
        container.addView(row3)
        card.addView(container)
        return card
    }

    private fun loadSummaryHeader() {
        val goals = goalPrefs.getGoals()
        val completed = goals.count { it.saved >= it.target }
        val totalSaved = goals.sumOf { it.saved }

        tvTotalGoals.text = "${goals.size}"
        tvCompletedGoals.text = "$completed done"
        tvTotalSaved.text = formatAmount(totalSaved)
    }

    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_goal, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etGoalTitle)
        val etTarget = dialogView.findViewById<EditText>(R.id.etGoalTarget)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emojiGrid)

        var selectedEmoji = goalEmojis[0]

        emojiGrid.columnCount = 4

        goalEmojis.forEachIndexed { i, emoji ->
            val tv = TextView(requireContext()).apply {
                text = emoji
                textSize = 24f
                gravity = Gravity.CENTER
                val size = dpToPx(56)
                layoutParams = GridLayout.LayoutParams().also {
                    it.width = size
                    it.height = size
                    it.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
                }
                setBackgroundResource(R.drawable.chip_unselected_bg)
                if (i == 0) setBackgroundResource(R.drawable.chip_selected_blue_bg)

                setOnClickListener {
                    selectedEmoji = emoji
                    for (j in 0 until emojiGrid.childCount) {
                        (emojiGrid.getChildAt(j) as? TextView)
                            ?.setBackgroundResource(R.drawable.chip_unselected_bg)
                    }
                    setBackgroundResource(R.drawable.chip_selected_blue_bg)
                }
            }
            emojiGrid.addView(tv)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Create New Goal")
            .setView(dialogView)
            .setPositiveButton("Create Goal", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val target = etTarget.text.toString().toDoubleOrNull()

                if (title.isEmpty()) {
                    etTitle.error = "Enter a goal title"
                    etTitle.requestFocus()
                    return@setOnClickListener
                }
                if (target == null || target <= 0) {
                    etTarget.error = "Enter a valid target amount"
                    etTarget.requestFocus()
                    return@setOnClickListener
                }

                val goal = Goal(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    target = target,
                    saved = 0.0,
                    emoji = selectedEmoji
                )
                goalPrefs.addGoal(goal)
                loadAll()
                Toast.makeText(requireContext(), "Goal created! 🎯", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showAddMoneyDialog(goal: Goal) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_money, null)

        val etAmount = dialogView.findViewById<EditText>(R.id.etAddAmount)
        val tvGoalName = dialogView.findViewById<TextView>(R.id.tvGoalName)
        val tvCurrentProgress = dialogView.findViewById<TextView>(R.id.tvCurrentProgress)

        tvGoalName.text = "${goal.emoji} ${goal.title}"
        tvCurrentProgress.text = "${formatAmount(goal.saved)} saved of ${formatAmount(goal.target)}"

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Add Money to Goal")
            .setView(dialogView)
            .setPositiveButton("Add Money", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val amount = etAmount.text.toString().toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    etAmount.error = "Enter a valid amount"
                    etAmount.requestFocus()
                    return@setOnClickListener
                }

                goalPrefs.addMoneyToGoal(goal.id, amount)
                loadAll()

                val updated = goalPrefs.getGoals().find { it.id == goal.id }
                val newPct = if (updated != null && updated.target > 0)
                    ((updated.saved / updated.target) * 100).toInt() else 0

                val msg = if (newPct >= 100) "🎉 Goal achieved!" else "Added ${formatAmount(amount)} to ${goal.title}!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showDeleteGoalDialog(goal: Goal) {
        AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Delete Goal")
            .setMessage("Delete \"${goal.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                goalPrefs.deleteGoal(goal.id)
                loadAll()
                Toast.makeText(requireContext(), "Goal deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditMonthlyGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_monthly_goal, null)

        val etTarget = dialogView.findViewById<EditText>(R.id.etMonthlyTarget)
        etTarget.setText(prefs.goalTarget.toInt().toString())
        etTarget.setSelection(etTarget.text.length)

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
            .setTitle("Edit Monthly Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val target = etTarget.text.toString().toDoubleOrNull()
                if (target == null || target <= 0) {
                    etTarget.error = "Enter a valid amount"
                    etTarget.requestFocus()
                    return@setOnClickListener
                }
                prefs.goalTarget = target
                loadAll()
                Toast.makeText(requireContext(), "Monthly goal updated!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun buildCircleBg(color: Int, alpha: Int = 40): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            setColor(Color.argb(alpha, r, g, b))
        }
    }

    private fun formatAmount(amount: Double): String {
        return currencyManager.formatAmount(amount)
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}