package com.hustlers.fintrack.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.hustlers.fintrack.R
import com.hustlers.fintrack.dataclass.Transaction
import com.hustlers.fintrack.storage.FinTrackPreferences
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var prefs: FinTrackPreferences

    private lateinit var cardIncome: CardView
    private lateinit var cardExpense: CardView
    private lateinit var tvIncomeToggle: TextView
    private lateinit var tvExpenseToggle: TextView


    private lateinit var categoryContainer: LinearLayout

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etNote: EditText

    private lateinit var btnBack: TextView
    private lateinit var btnSave: TextView

    private var isIncome = false
    private var selectedCategory = ""
    private var selectedIcon = "💳"

    private val expenseCategories = listOf(
        "🍔" to "Food",
        "🛍" to "Shopping",
        "⛽" to "Transport",
        "📺" to "Bills",
        "🏥" to "Health",
        "🎓" to "Education",
        "🎮" to "Entertainment",
        "🏠" to "Rent",
        "📦" to "Other"
    )

    private val incomeCategories = listOf(
        "💰" to "Salary",
        "💸" to "Freelance",
        "🎁" to "Gift",
        "📈" to "Investment",
        "🏦" to "Bank",
        "💡" to "Bonus",
        "📦" to "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        prefs = FinTrackPreferences.getInstance(this)

        bindViews()
        setupTypeToggle()
        setupCategoryChips(expenseCategories)
        setupSave()

        btnBack.setOnClickListener { finish() }
    }

    private fun bindViews() {
        cardIncome        = findViewById(R.id.cardIncomeToggle)
        cardExpense       = findViewById(R.id.cardExpenseToggle)
        tvIncomeToggle    = findViewById(R.id.tvIncomeToggle)
        tvExpenseToggle   = findViewById(R.id.tvExpenseToggle)
        categoryContainer = findViewById(R.id.categoryContainer)
        etTitle           = findViewById(R.id.etTitle)
        etAmount          = findViewById(R.id.etAmount)
        etNote            = findViewById(R.id.etNote)
        btnBack           = findViewById(R.id.btnBack)
        btnSave           = findViewById(R.id.btnSave)
    }


    private fun setupTypeToggle() {
        selectExpense()

        cardIncome.setOnClickListener  { selectIncome()  }
        cardExpense.setOnClickListener { selectExpense() }
    }

    private fun selectIncome() {
        isIncome = true

        cardIncome.setCardBackgroundColor(0xFF4ADE80.toInt())
        tvIncomeToggle.setTextColor(0xFF0A0A0A.toInt())

        cardExpense.setCardBackgroundColor(0x00000000)
        tvExpenseToggle.setTextColor(0x99FFFFFF.toInt())

        setupCategoryChips(incomeCategories)

        etAmount.setHintTextColor(0x664ADE80.toInt())
    }

    private fun selectExpense() {
        isIncome = false

        cardExpense.setCardBackgroundColor(0xFFF87171.toInt())
        tvExpenseToggle.setTextColor(0xFF0A0A0A.toInt())

        cardIncome.setCardBackgroundColor(0x00000000)
        tvIncomeToggle.setTextColor(0x99FFFFFF.toInt())

        setupCategoryChips(expenseCategories)

        etAmount.setHintTextColor(0x66F87171.toInt())
    }


    private fun setupCategoryChips(categories: List<Pair<String, String>>) {
        categoryContainer.removeAllViews()
        selectedCategory = ""
        selectedIcon = "💳"

        var rowLayout: LinearLayout? = null

        categories.forEachIndexed { index, (emoji, name) ->
            if (index % 4 == 0) {
                rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = dpToPx(8) }
                }
                categoryContainer.addView(rowLayout)
            }

            val chip = TextView(this).apply {
                text = "$emoji $name"
                textSize = 12f
                setTextColor(0xCCFFFFFF.toInt())
                setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
                setBackgroundResource(R.drawable.chip_unselected_bg)
                tag = "$emoji|$name"

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = dpToPx(8) }
                layoutParams = params

                setOnClickListener { selectChip(this, emoji, name) }
            }

            rowLayout?.addView(chip)
        }

        val firstChip = (categoryContainer.getChildAt(0) as? LinearLayout)
            ?.getChildAt(0) as? TextView
        firstChip?.let {
            val parts = it.tag.toString().split("|")
            selectChip(it, parts[0], parts[1])
        }
    }

    private fun selectChip(chip: TextView, emoji: String, name: String) {

        for (i in 0 until categoryContainer.childCount) {
            val row = categoryContainer.getChildAt(i) as? LinearLayout ?: continue
            for (j in 0 until row.childCount) {
                val c = row.getChildAt(j) as? TextView ?: continue
                c.setBackgroundResource(R.drawable.chip_unselected_bg)
                c.setTextColor(0xCCFFFFFF.toInt())
            }
        }

        chip.setBackgroundResource(
            if (isIncome) R.drawable.chip_selected_green_bg
            else R.drawable.chip_selected_red_bg
        )
        chip.setTextColor(0xFF0A0A0A.toInt())

        selectedCategory = name
        selectedIcon = emoji
    }


    private fun setupSave() {
        btnSave.setOnClickListener {
            val title  = etTitle.text.toString().trim()
            val amtStr = etAmount.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Enter a title"
                etTitle.requestFocus()
                return@setOnClickListener
            }
            if (amtStr.isEmpty()) {
                etAmount.error = "Enter amount"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            val rawAmount = amtStr.toDoubleOrNull()
            if (rawAmount == null || rawAmount <= 0) {
                etAmount.error = "Enter a valid amount"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (selectedCategory.isEmpty()) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalAmount = if (isIncome) rawAmount else -rawAmount
            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            val note = etNote.text.toString().trim()
            val displayTitle = if (note.isNotEmpty()) title else title

            val transaction = Transaction(
                title    = displayTitle,
                category = selectedCategory,
                amount   = finalAmount,
                icon     = selectedIcon,
                date     = dateStr
            )

            prefs.addTransaction(transaction)

            Toast.makeText(this, "✓ Transaction saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}