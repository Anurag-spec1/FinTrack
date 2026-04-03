package com.hustlers.fintrack.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hustlers.fintrack.dataclass.Transaction

class FinTrackPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    var balance: Double
        get() = prefs.getString(KEY_BALANCE, "0.0")!!.toDouble()
        set(value) = prefs.edit().putString(KEY_BALANCE, value.toString()).apply()

    var totalIncome: Double
        get() = prefs.getString(KEY_INCOME, "0.0")!!.toDouble()
        set(value) = prefs.edit().putString(KEY_INCOME, value.toString()).apply()

    var totalExpenses: Double
        get() = prefs.getString(KEY_EXPENSES, "0.0")!!.toDouble()
        set(value) = prefs.edit().putString(KEY_EXPENSES, value.toString()).apply()

    val totalSavings: Double
        get() = totalIncome - totalExpenses

    var goalTarget: Double
        get() = prefs.getString(KEY_GOAL_TARGET, "10000.0")!!.toDouble()
        set(value) = prefs.edit().putString(KEY_GOAL_TARGET, value.toString()).apply()

    val goalProgress: Int
        get() {
            if (goalTarget <= 0) return 0
            return ((totalSavings / goalTarget) * 100).toInt().coerceIn(0, 100)
        }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "User")!!
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()


    fun getTransactions(): MutableList<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveTransactions(transactions: List<Transaction>) {
        prefs.edit().putString(KEY_TRANSACTIONS, gson.toJson(transactions)).apply()
    }

    fun addTransaction(transaction: Transaction) {
        val list = getTransactions()
        list.add(0, transaction)
        saveTransactions(list)

        if (transaction.amount >= 0) {
            totalIncome += transaction.amount
            balance += transaction.amount
        } else {
            totalExpenses += (-transaction.amount)
            balance += transaction.amount
        }
    }

    fun deleteTransaction(transactionId: String) {
        val list = getTransactions()
        val txn = list.find { it.id == transactionId } ?: return
        list.remove(txn)
        saveTransactions(list)

        if (txn.amount >= 0) {
            totalIncome = (totalIncome - txn.amount).coerceAtLeast(0.0)
            balance -= txn.amount
        } else {
            totalExpenses = (totalExpenses - (-txn.amount)).coerceAtLeast(0.0)
            balance -= txn.amount
        }
    }

    fun getRecentTransactions(limit: Int = 5): List<Transaction> =
        getTransactions().take(limit)

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val PREF_NAME = "fintrack_prefs"

        private const val KEY_BALANCE      = "balance"
        private const val KEY_INCOME       = "total_income"
        private const val KEY_EXPENSES     = "total_expenses"
        private const val KEY_GOAL_TARGET  = "goal_target"
        private const val KEY_USER_NAME    = "user_name"
        private const val KEY_TRANSACTIONS = "transactions"

        @Volatile private var instance: FinTrackPreferences? = null

        fun getInstance(context: Context): FinTrackPreferences =
            instance ?: synchronized(this) {
                instance ?: FinTrackPreferences(context).also { instance = it }
            }
    }
}