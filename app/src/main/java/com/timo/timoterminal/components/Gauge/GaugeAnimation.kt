package com.timo.timoterminal.components.Gauge

import android.view.animation.Animation
import android.view.animation.Transformation

class GaugeAnimation(
    private val gauge: Gauge,
    percentage: Float,
    beginFrom0: Boolean? = false
) : Animation() {

    private val newAngle: Float =  (percentage / 100) * 180f
    private val oldAngle: Float = this.gauge.arcAngle

    var onAnimationEnd: (() -> Unit)? = null

    init {
        this.duration = 1000
        setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                onAnimationEnd?.invoke()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        var angle = (newAngle - oldAngle) * interpolatedTime
        if (angle < 0) {
            angle = -angle
        }
        gauge.arcAngle = angle
        gauge.requestLayout()
    }
}