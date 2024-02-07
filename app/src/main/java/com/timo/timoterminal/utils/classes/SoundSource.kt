package com.timo.timoterminal.utils.classes

import android.content.Context
import android.media.SoundPool
import com.timo.timoterminal.R

class SoundSource(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()

    init {
        soundPool.load(context, R.raw.beep, 1)
    }

    fun beep() {
        soundPool.play(1, 1f, 1f, 1, 0, 1f)
    }
}