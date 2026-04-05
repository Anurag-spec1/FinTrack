package com.hustlers.fintrack.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var labels = listOf<String>()
    private var values = listOf<Float>()

    private val bgBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x22FFFFFF
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x99FFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 28f
    }

    private val barColors = listOf(
        0xFFF87171.toInt(),
        0xFFFBBF24.toInt(),
        0xFF60A5FA.toInt(),
        0xFFA78BFA.toInt(),
        0xFF34D399.toInt()
    )

    fun setData(labels: List<String>, values: List<Float>) {
        this.labels = labels
        this.values = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) {
            labelPaint.color = 0x66FFFFFF.toInt()
            canvas.drawText("No data", width / 2f, height / 2f, labelPaint)
            return
        }

        val maxVal  = values.maxOrNull() ?: 1f
        val count   = values.size
        val bottomPad = 50f
        val topPad    = 20f
        val chartH  = height - bottomPad - topPad
        val slotW   = width.toFloat() / count
        val barW    = slotW * 0.45f

        values.forEachIndexed { i, value ->
            val cx     = slotW * i + slotW / 2f
            val barH   = (value / maxVal) * chartH
            val top    = height - bottomPad - barH
            val bottom = height - bottomPad
            val left   = cx - barW / 2
            val right  = cx + barW / 2

            canvas.drawRoundRect(left, topPad, right, bottom, 10f, 10f, bgBarPaint)

            barPaint.color = barColors[i % barColors.size]
            canvas.drawRoundRect(left, top, right, bottom, 10f, 10f, barPaint)

            labelPaint.color = 0x99FFFFFF.toInt()
            labelPaint.textSize = 26f
            canvas.drawText(
                labels.getOrElse(i) { "" }.take(6),
                cx, height - 12f, labelPaint
            )
        }
    }
}