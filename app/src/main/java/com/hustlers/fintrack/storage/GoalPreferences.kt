package com.hustlers.fintrack.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hustlers.fintrack.dataclass.Goal

class GoalPreferences private constructor(context: Context) {


    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun getGoals(): MutableList<Goal> {
        val json = prefs.getString(KEY_GOALS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Goal>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveGoals(goals: List<Goal>) {
        prefs.edit().putString(KEY_GOALS, gson.toJson(goals)).apply()
    }

    fun addGoal(goal: Goal) {
        val list = getGoals()
        list.add(goal)
        saveGoals(list)
    }

    fun deleteGoal(id: String) {
        val list = getGoals()
        list.removeAll { it.id == id }
        saveGoals(list)
    }

    fun addMoneyToGoal(id: String, amount: Double) {
        val list = getGoals()
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return

        val goal = list[index]
        val newSaved = (goal.saved + amount).coerceAtMost(goal.target)
        list[index] = goal.copy(saved = newSaved)
        saveGoals(list)
    }

    fun updateGoalTarget(id: String, newTarget: Double) {
        val list = getGoals()
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return
        list[index] = list[index].copy(target = newTarget)
        saveGoals(list)
    }

    companion object {
        private const val PREF_NAME = "fintrack_goals"
        private const val KEY_GOALS = "goals"

        @Volatile
        private var instance: GoalPreferences? = null

        fun getInstance(context: Context): GoalPreferences =
            instance ?: synchronized(this) {
                instance ?: GoalPreferences(context).also { instance = it }
            }
    }
}