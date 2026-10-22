package com.example.iottemp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/** Gráfico de línea simple (sin librerías externas). Muestra las lecturas y el umbral. */
class SimpleLineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Double> = emptyList()
    private var threshold: Double? = null
    private val d = resources.displayMetrics.density

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0"); strokeWidth = 2.5f * d
        style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND
    }
    private val gridPaint = Paint().apply { color = Color.parseColor("#33888888"); strokeWidth = 1f }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY; textSize = 11f * d }
    private val thrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED; strokeWidth = 1.5f * d; style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f * d, 6f * d), 0f)
    }

    fun setData(values: List<Double>, threshold: Double?) {
        this.values = values
        this.threshold = threshold
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return
        val left = 40f * d
        val right = width - 8f * d
        val top = 8f * d
        val bottom = height - 8f * d

        var lo = values.min()
        var hi = values.max()
        threshold?.let { lo = minOf(lo, it); hi = maxOf(hi, it) }
        lo -= 1.0
        hi += 1.0

        fun yOf(v: Double): Float = bottom - ((v - lo) / (hi - lo)).toFloat() * (bottom - top)
        fun xOf(i: Int): Float =
            if (values.size == 1) (left + right) / 2 else left + i * (right - left) / (values.size - 1)

        for (k in 0..3) {
            val gv = lo + (hi - lo) * k / 3
            val gy = yOf(gv)
            canvas.drawLine(left, gy, right, gy, gridPaint)
            canvas.drawText("%.0f".format(gv), 4f * d, gy + 4f * d, textPaint)
        }
        threshold?.let { val ty = yOf(it); canvas.drawLine(left, ty, right, ty, thrPaint) }

        val path = Path()
        values.forEachIndexed { i, v ->
            if (i == 0) path.moveTo(xOf(i), yOf(v)) else path.lineTo(xOf(i), yOf(v))
        }
        canvas.drawPath(path, linePaint)
    }
}
