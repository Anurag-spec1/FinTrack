package com.hustlers.fintrack.dataclass

data class Goal(
    val id: String,
    val title: String,
    val target: Double,
    val saved: Double,
    val emoji: String = "🎯"
)
