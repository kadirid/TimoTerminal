package com.timo.timoterminal.components.Barplot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.android.material.color.MaterialColors
import com.timo.timoterminal.R
import com.timo.timoterminal.utils.classes.BGData
import kotlin.math.log

class Barplot(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var data = ArrayList<BGData>();

    private val paint = Paint().apply {
        val col = MaterialColors.getColor(
            context,
            R.attr.colorSurfaceContainerHighest,
            getResources().getColor(R.color.black)
        )
        style = Paint.Style.FILL
        strokeWidth = 10f
        color = col
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
            centerX - rectWidth,
            centerY - rectHeight,
            centerX + rectWidth,
            centerY + rectHeight
        )
    }


    fun setData(data : ArrayList<BGData>) {
        //we are not allowed to draw anything outside the onDraw method
        this.data = data
        invalidate()
    }

    private fun createRoundedRectPath(rect: RectF, rx: Float, ry: Float, roundedRight: Boolean, roundedLeft: Boolean): Path {
        val path = Path()
        if (roundedRight) {
            path.moveTo(rect.left, rect.top)
            path.lineTo(rect.right - rx, rect.top)
            path.quadTo(rect.right, rect.top, rect.right, rect.top + ry)
            path.lineTo(rect.right, rect.bottom - ry)
            path.quadTo(rect.right, rect.bottom, rect.right - rx, rect.bottom)
            path.lineTo(rect.left, rect.bottom)
        } else if (roundedLeft) {
            path.moveTo(rect.left + rx, rect.top)
            path.quadTo(rect.left, rect.top, rect.left, rect.top + ry)
            path.lineTo(rect.left, rect.bottom - ry)
            path.quadTo(rect.left, rect.bottom, rect.left + rx, rect.bottom)
            path.lineTo(rect.right, rect.bottom)
            path.lineTo(rect.right, rect.top)
        } else {
            path.addRect(rect, Path.Direction.CW)
        }
        path.close()
        return path
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = height / 3f
        paint.strokeWidth = height.toFloat()
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        if (data.isNotEmpty()) {
            val sum = data.map { it.data }.sum()
            var lastThreshold = 0f
            var index = 0
            data.forEach {
                paint.color = it.color
                val length = (it.data / sum) * width
                rect.set(lastThreshold, 0f, lastThreshold + length, height.toFloat())
                if (data.size == 1) {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                } else if (index == 0) {
                    // draw first rectangle with rounded corners on the left side
                    canvas.drawPath(createRoundedRectPath(rect, cornerRadius, cornerRadius, false, true), paint)
                } else if (index == data.size - 1) {
                    // draw last rectangle with rounded corners on the right side
                    canvas.drawPath(createRoundedRectPath(rect, cornerRadius, cornerRadius, true, false), paint)
                } else {
                    // draw other rectangles normally
                    canvas.drawPath(createRoundedRectPath(rect, cornerRadius, cornerRadius, false, false), paint)
                }
                lastThreshold += length
                index++
            }
        }
    }
}