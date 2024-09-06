package com.timo.timoterminal.components.Gauge

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import com.timo.timoterminal.R
import com.timo.timoterminal.utils.classes.BGData

class Gauge(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private var ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.Gauge)
    private var data : ArrayList<BGData> = ArrayList()
    var arcAngle: Float = (ta.getFloat(R.styleable.Gauge_percentage, 0f) / 100) * 180f
    private var startAngle = 180f

    private val paint = Paint().apply {
        val col = MaterialColors.getColor(
            context,
            R.attr.colorSurfaceContainerHighest,
            resources.getColor(R.color.black)
        )
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = col
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val percentagePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = 0xFF00FF00.toInt() // Green color
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val pPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = 0xFFFFB900.toInt()
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val rect = RectF(
        0f,
        0f,
        width.toFloat(),
        height.toFloat()
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val centerX = w / 2f
        val centerY = h / 2f

        val rectWidth = width.toFloat() - 20f
        val rectHeight = height.toFloat() - 20f

        rect.set(
            centerX - rectWidth / 2,
            centerY - rectHeight / 2,
            centerX + rectWidth / 2,
            centerY + rectHeight / 2
        )
    }

    /**
     * This Method sets the data and the colors for the gauge. Animation is working for one color only.
     * @param data HashMap<Float, Int> - The data for the gauge. The key is the percentage and the value is the color.
     */
    fun setData(data : ArrayList<BGData>) {
        //we are not allowed to draw anything outside the onDraw method
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(rect, 180f, 180f, false, paint)
        if (data.isNotEmpty()) {
            val sum = data.map { it.data }.sum()
            var lastThreshold = 0f
            var index = 0
            data.reversed().forEach {
                pPaint.color = it.color
                val angle = (it.data / sum) * 180f
                canvas.drawArc(rect, lastThreshold, -angle, false, pPaint)
                lastThreshold -= angle
                index++
            }
        } else {
            canvas.drawArc(rect, startAngle, arcAngle, false, percentagePaint)
        }

    }
}