package com.hustlers.fintrack.dataclass

import java.util.UUID

data class Transaction(
    val title: String,
    val category: String,
    val amount: Double,
    val icon: String,
    val date: String,
    val id: String = UUID.randomUUID().toString()
)