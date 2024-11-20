package com.timo.timoterminal.service

import android.graphics.Point
import android.service.dreams.DreamService
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.timo.timoterminal.R
import kotlin.random.Random

class AnimatedDreamSaverService : DreamService() {
    private var animator: ViewPropertyAnimator? = null
    private lateinit var image: ImageView
    private val rand = Random.Default.nextInt(4)
    private var x = 0
    private var y = 0
    private var up = rand % 2 == 0
    private var right = rand > 1
    private var maxX = 0
    private var maxY = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setContentView(R.layout.animated_screen_save)
        image = findViewById(R.id.image_view_jumping_logo)
        isInteractive = false
        isFullscreen = true

        val mdisp = windowManager.defaultDisplay
        val mdispSize = Point()
        mdisp.getSize(mdispSize)
        maxX = mdispSize.x - 117
        maxY = mdispSize.y - 50
        x = mdispSize.x / 2
        y = mdispSize.y / 2

        animator = image.animate()
        animator!!.setInterpolator(LinearInterpolator())
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun startAnimation() {
        if (animator != null) {
            animator!!.withEndAction {
                up = if (up) y != 49 else y == maxY
                right = if (right) x != maxX else x == 116
                startAnimation()
            }

            val distanceX = if (right) maxX - x else x - 116
            val distanceY = if (up) y - 49 else maxY - y
            val movement = if (distanceX < distanceY) distanceX else distanceY
            val duration = movement * 15

            animator!!.xBy((if (right) movement else -movement).toFloat())
            animator!!.yBy((if (up) -movement else movement).toFloat())
            if (right) x += movement else x -= movement
            if (up) y -= movement else y += movement
            animator!!.setDuration(duration.toLong())
            animator!!.start()
        }
    }
}
