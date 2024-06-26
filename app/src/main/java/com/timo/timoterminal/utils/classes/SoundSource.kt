package com.timo.timoterminal.utils.classes

import android.content.Context
import android.media.SoundPool
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.timo.timoterminal.R
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.SharedPrefService

class SoundSource(val sharedPrefService: SharedPrefService, val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(1).build()
    private val map = HashMap<Int, Int>()
    private val soundThread = HandlerThread("soundThread")
    private var poolNo = 0
    private var handler: Handler

    init {
        if (!soundThread.isAlive) soundThread.start()
        handler = Handler(soundThread.looper)
        loadDefault()
        soundPool.setOnLoadCompleteListener(null)
    }

    fun playSound(soundId: Int) {
        if (sharedPrefService.getBoolean(SharedPreferenceKeys.SOUND_ACTIVITY, true)) {
            handler.post {
                if (map[soundId] != null && map[soundId] != -1) {
                    soundPool.play(map[soundId]!!, 0.5f, 0.5f, 1, 0, 1f)
                } else {
                    Log.d("Sound missed", soundId.toString())
                }
            }
        }
    }

    private fun unload() {
        for (c in map.keys) {
            if (map[c] != null && map[c] != -1 && c != successSound && c != failedSound && c != authenticationFailed) {
                soundPool.unload(map[c]!!)
                map[c] = -1
            }
        }
    }

    private fun loadDefault() {
        handler.post {
            soundPool.setOnLoadCompleteListener(null)
            map[successSound] = soundPool.load(context, R.raw.buzzer_successfull, 1)
            map[failedSound] = soundPool.load(context, R.raw.buzzer_faild, 1)
            val lang = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)
            if (lang == "de") {
                map[authenticationFailed] =
                    soundPool.load(context, R.raw.de_authentification_failed, 1)
            } else {
                map[authenticationFailed] =
                    soundPool.load(context, R.raw.en_authentification_failed, 1)
            }
        }
    }

    private fun isLoaded(soundId: Int): Boolean {
        return map[soundId] != null && map[soundId] != -1
    }

    fun loadForAttendance() {
        handler.post {
            if (isLoaded(offlineSaved)) {
                return@post
            }
            unload()
            poolNo = 2
            soundPool.setOnLoadCompleteListener(null)

            val lang = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)
            if (lang == "de") {
                map[offlineSaved] = soundPool.load(context, R.raw.de_offline_saved, 1)
            } else {
                map[offlineSaved] = soundPool.load(context, R.raw.en_offline_saved, 1)
            }
        }
    }

    fun loadForLogin(lang: String) {
        handler.post {
            if (isLoaded(loginFailed)) {
                return@post
            }
            unload()
            poolNo = 1
            soundPool.setOnLoadCompleteListener(null)

            if (lang.toLowerCase(Locale.current).startsWith("de")) {
                map[loginFailed] = soundPool.load(context, R.raw.de_login_failed, 1)
                map[loginSuccessful] = soundPool.load(context, R.raw.de_login_successful, 1)
            } else {
                map[loginFailed] = soundPool.load(context, R.raw.en_login_failed, 1)
                map[loginSuccessful] = soundPool.load(context, R.raw.en_login_successful, 1)
            }
        }
    }

    fun loadForFP(finger: Int = -1) {
        handler.post {
            if (isLoaded(fingerSaved)) {
                return@post
            }
            unload()
            poolNo = 3
            soundPool.setOnLoadCompleteListener(null)

            val lang = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)
            if (lang == "de") {
                map[fingerSaved] = soundPool.load(context, R.raw.de_finger_saved, 1)
                map[placeSameFingerAgain] = soundPool.load(
                    context, R.raw.de_please_place_the_same_finger_on_the_scanner_again, 1
                )
                if (finger == 3 || finger == -1) {
                    map[placeLeftIndexFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_left_index_finger_on_the_scanner, 1
                    )
                }
                if (finger == 0 || finger == -1) {
                    map[placeLeftLittleFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_left_little_finger_on_the_scanner, 1
                    )
                }
                if (finger == 2 || finger == -1) {
                    map[placeLeftMiddleFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_left_middle_finger_on_the_scanner, 1
                    )
                }
                if (finger == 1 || finger == -1) {
                    map[placeLeftRingFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_left_ring_finger_on_the_scanner, 1
                    )
                }
                if (finger == 4 || finger == -1) {
                    map[placeLeftThumb] =
                        soundPool.load(
                            context,
                            R.raw.de_please_place_your_left_thumb_on_the_scanner,
                            1
                        )
                }
                if (finger == 6 || finger == -1) {
                    map[placeRightIndexFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_right_index_finger_on_the_scanner, 1
                    )
                }
                if (finger == 9 || finger == -1) {
                    map[placeRightLittleFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_right_little_finger_on_the_scanner, 1
                    )
                }
                if (finger == 7 || finger == -1) {
                    map[placeRightMiddleFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_right_middle_finger_on_the_scanner, 1
                    )
                }
                if (finger == 8 || finger == -1) {
                    map[placeRightRingFinger] = soundPool.load(
                        context, R.raw.de_please_place_your_right_ring_finger_on_the_scanner, 1
                    )
                }
                if (finger == 5 || finger == -1) {
                    map[placeRightThumb] =
                        soundPool.load(
                            context,
                            R.raw.de_please_place_your_right_thumb_on_the_scanner,
                            1
                        )
                }
                map[selectFinger] = soundPool.load(context, R.raw.de_please_select_finger, 1)
                map[takeFingerAway] =
                    soundPool.load(context, R.raw.de_please_take_your_finger_away, 1)
                map[takeFingerAwayAndPutItOnAgain] = soundPool.load(
                    context,
                    R.raw.de_please_take_your_finger_away_and_place_it_on_again,
                    1
                )
            } else {
                map[fingerSaved] = soundPool.load(context, R.raw.en_finger_saved, 1)
                map[placeSameFingerAgain] = soundPool.load(
                    context, R.raw.en_please_place_the_same_finger_on_the_scanner_again, 1
                )
                if (finger == 3 || finger == -1) {
                    map[placeLeftIndexFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_left_index_finger_on_the_scanner, 1
                    )
                }
                if (finger == 0 || finger == -1) {
                    map[placeLeftLittleFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_left_little_finger_on_the_scanner, 1
                    )
                }
                if (finger == 2 || finger == -1) {
                    map[placeLeftMiddleFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_left_middle_finger_on_the_scanner, 1
                    )
                }
                if (finger == 1 || finger == -1) {
                    map[placeLeftRingFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_left_ring_finger_on_the_scanner, 1
                    )
                }
                if (finger == 4 || finger == -1) {
                    map[placeLeftThumb] =
                        soundPool.load(
                            context,
                            R.raw.en_please_place_your_left_thumb_on_the_scanner,
                            1
                        )
                }
                if (finger == 6 || finger == -1) {
                    map[placeRightIndexFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_right_index_finger_on_the_scanner, 1
                    )
                }
                if (finger == 9 || finger == -1) {
                    map[placeRightLittleFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_right_little_finger_on_the_scanner, 1
                    )
                }
                if (finger == 7 || finger == -1) {
                    map[placeRightMiddleFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_right_middle_finger_on_the_scanner, 1
                    )
                }
                if (finger == 8 || finger == -1) {
                    map[placeRightRingFinger] = soundPool.load(
                        context, R.raw.en_please_place_your_right_ring_finger_on_the_scanner, 1
                    )
                }
                if (finger == 5 || finger == -1) {
                    map[placeRightThumb] =
                        soundPool.load(
                            context,
                            R.raw.en_please_place_your_right_thumb_on_the_scanner,
                            1
                        )
                }
                map[selectFinger] = soundPool.load(context, R.raw.en_please_select_finger, 1)
                map[takeFingerAway] =
                    soundPool.load(context, R.raw.en_please_take_your_finger_away, 1)
                map[takeFingerAwayAndPutItOnAgain] = soundPool.load(
                    context,
                    R.raw.en_please_take_your_finger_away_and_place_it_on_again,
                    1
                )
            }
        }
    }

    companion object {
        const val successSound = 0
        const val failedSound = 1
        const val authenticationFailed = 2// ok
//        const val enterPIN = 3
        const val fingerSaved = 4// ok
        const val loginFailed = 6// ok
        const val loginSuccessful = 7// ok
        const val offlineSaved = 8// ok
        const val placeSameFingerAgain = 9// ok
        const val placeFingerBase = 10// ok
        const val placeLeftLittleFinger = placeFingerBase + 0//10
        const val placeLeftRingFinger = placeFingerBase + 1//11
        const val placeLeftMiddleFinger = placeFingerBase + 2//12
        const val placeLeftIndexFinger = placeFingerBase + 3//13
        const val placeLeftThumb = placeFingerBase + 4//14
        const val placeRightThumb = placeFingerBase + 5//15
        const val placeRightIndexFinger = placeFingerBase + 6//16
        const val placeRightMiddleFinger = placeFingerBase + 7//17
        const val placeRightRingFinger = placeFingerBase + 8//18
        const val placeRightLittleFinger = placeFingerBase + 9//19
        const val selectFinger = 20// ok
        const val takeFingerAway = 21// ok
        const val takeFingerAwayAndPutItOnAgain = 22// ok
//        const val savedSuccessfully = 23
//        const val savingError = 24
    }

    fun reloadForLanguage() {
        handler.post {
            val lang = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE)
            unload()
            if (lang != null) {
                when (poolNo) {
                    1 -> loadForLogin(lang)
                    2 -> loadForAttendance()
                    3 -> loadForFP()
                }
                soundPool.unload(map[authenticationFailed]!!)
                if (lang == "de") {
                    map[authenticationFailed] =
                        soundPool.load(context, R.raw.de_authentification_failed, 1)
                } else {
                    map[authenticationFailed] =
                        soundPool.load(context, R.raw.en_authentification_failed, 1)
                }
            }
        }
    }
}