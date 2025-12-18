package com.timo.timoterminal.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.lcdk.data.IFingerprintListener
import kotlinx.coroutines.launch

class MBSheetVerifyUserViewModel(
    private val userRepository: UserRepository,
    private val soundSource: SoundSource
): ViewModel(), TimoRfidListener, IFingerprintListener {
    val liveUser = MutableLiveData<UserEntity?>(null)

    fun getUserByPIN(pin: String) {
        viewModelScope.launch {
            val users = userRepository.getEntityByPIN(pin)
            if (users.isNotEmpty()) {
                val user = users[0]
                soundSource.playSound(SoundSource.successSound)
                liveUser.postValue(user)
            } else {
                soundSource.playSound(SoundSource.failedSound)
            }
        }
    }

    override fun onRfidRead(text: String) {
        viewModelScope.launch {
            val rfidCode = text.toLongOrNull(16)
            if (rfidCode != null) {
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                val users = userRepository.getEntityByCard(oct)
                if (users.isNotEmpty()) {
                    val user = users[0]
                    soundSource.playSound(SoundSource.successSound)
                    liveUser.postValue(user)
                } else {
                    soundSource.playSound(SoundSource.failedSound)
                }
            } else {
                soundSource.playSound(SoundSource.failedSound)
            }
        }
    }

    override fun onFingerprintPressed(template: ByteArray): Boolean {
        viewModelScope.launch {
            // get Key associated to the fingerprint
            MainApplication.lcdk.identifyFingerPrint(template, 70).run {
                if (this.isNotEmpty()) {
                    val id = this.substring(0, this.length - 2).toLong()
                    val users = userRepository.getEntity(id)
                    if (users.isNotEmpty()) {
                        val user = users[0]
                        soundSource.playSound(SoundSource.successSound)
                        liveUser.postValue(user)
                    }
                    return@launch
                } else {
                    soundSource.playSound(SoundSource.failedSound)
                }
            }
        }
        return true
    }

    override fun onFingerprintPressed(template: ByteArray, bmp: Bitmap?) {}
}