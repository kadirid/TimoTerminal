package com.timo.timoterminal.utils

import com.zkteco.android.core.interfaces.RfidListener
import kotlin.jvm.internal.Intrinsics

@JvmDefaultWithoutCompatibility
interface TimoRfidListener : RfidListener {

    override fun onRfidReadWithOldValue(
        rfidInfo: String,
        oldRfidInfo: String,
        useRfidStandardization: Boolean
    ) {
        Intrinsics.checkNotNullParameter(rfidInfo, "rfidInfo")
        Intrinsics.checkNotNullParameter(oldRfidInfo, "oldRfidInfo")
        onRfidRead(oldRfidInfo)
    }
}