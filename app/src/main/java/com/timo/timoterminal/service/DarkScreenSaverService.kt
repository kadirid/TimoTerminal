package com.timo.timoterminal.service

import android.service.dreams.DreamService
import com.timo.timoterminal.R

class DarkScreenSaverService: DreamService() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setContentView(R.layout.dark_screen_saver)
        isInteractive = false
        isFullscreen = true
    }
}