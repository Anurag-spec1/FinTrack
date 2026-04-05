package com.hustlers.fintrack.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Segment(val sweep: Float, val color: Int)

    private var segments = listOf<Segment>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val oval = RectF()
    private val GAP = 3f

    fun setData(data: List<Segment>) {
        segments = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val strokeW = width * 0.18f
        val r = (minOf(width, height) / 2f) - strokeW / 2f - dpToPx(4)

        oval.set(cx - r, cy - r, cx + r, cy + r)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeW
        paint.strokeCap = Paint.Cap.ROUND

        if (segments.isEmpty()) {
            paint.color = 0x22FFFFFF
            canvas.drawArc(oval, 0f, 360f, false, paint)

            labelPaint.textSize = width * 0.1f
            labelPaint.color = 0x66FFFFFF.toInt()
            canvas.drawText("No data", cx, cy + labelPaint.textSize / 3, labelPaint)
            return
        }

        var startAngle = -90f
        segments.forEach { seg ->
            paint.color = seg.color
            canvas.drawArc(oval, startAngle + GAP / 2, seg.sweep - GAP, false, paint)
            startAngle += seg.sweep
        }

        labelPaint.textSize = width * 0.09f
        labelPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText("Expenses", cx, cy - width * 0.05f, labelPaint)

        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = width * 0.1f
        canvas.drawText("${segments.size} cat.", cx, cy + width * 0.09f, labelPaint)
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}