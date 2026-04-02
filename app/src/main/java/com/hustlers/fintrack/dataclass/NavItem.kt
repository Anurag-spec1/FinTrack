package com.hustlers.fintrack.dataclass

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

data class NavItem(
    val container: LinearLayout,
    val icon: ImageView,
    val label: TextView
)
