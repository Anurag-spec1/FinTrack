package com.hustlers.fintrack.dataclass

data class Transaction(
    val title: String,
    val category: String,
    val amount: Double,
    val icon: String,
    val date: String
)