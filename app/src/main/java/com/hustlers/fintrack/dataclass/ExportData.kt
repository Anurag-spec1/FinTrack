package com.hustlers.fintrack.dataclass

data class ExportData(
    val version: Int,
    val exportDate: String,
    val userName: String,
    val userEmail: String,
    val userBio: String,
    val goalTarget: Double,
    val currency: String,
    val transactions: List<com.hustlers.fintrack.dataclass.Transaction>,
    val goals: List<com.hustlers.fintrack.dataclass.Goal>
)