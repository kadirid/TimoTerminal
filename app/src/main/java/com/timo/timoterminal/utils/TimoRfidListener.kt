package com.timo.timoterminal.utils

import com.zkteco.android.core.interfaces.RfidListener
import kotlin.jvm.internal.Intrinsics

interface TimoRfidListener : RfidListener {

    override fun onRfidReadWithOldValue(
        rfidInfo: String,
        oldRfidInfo: String,
        useRfidStandardization: Boolean
    ) {
        Intrinsics.checkNotNullParameter(rfidInfo, "rfidInfo")
        Intrinsics.checkNotNullParameter(oldRfidInfo, "oldRfidInfo")
        if (rfidInfo == oldRfidInfo.removePrefix("00")) {
            onRfidRead(oldRfidInfo)
            return
        }
        val newByte = Utils.hexStringToByteArray(rfidInfo)
        val oldByte = Utils.hexStringToByteArray(oldRfidInfo)
        if (newByte.size == oldByte.size && newByte[0] == oldByte[oldByte.size - 1]) {
            onRfidRead(oldRfidInfo)
            return
        }
        if (oldRfidInfo.contains(rfidInfo)) {
            var c = oldByte[0]
            if (c in 0..15) {
                if(c>= 8){
                    c = (c-8).toByte()
                }
                oldByte[0] = c
                onRfidRead(Utils.byteArrayToHex(oldByte))
                return
            }
        }
        onRfidRead(oldRfidInfo)
    }
}