package com.timo.timoterminal.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryReceiver(private val callback: BatteryStatusCallback) : BroadcastReceiver() {
    interface BatteryStatusCallback {
        fun onBatteryStatusChanged(batteryPercentage: Int, usbCharge: Boolean, plugCharge: Boolean)
    }

    override fun onReceive(p0: Context, p1: Intent) {
        val batteryLevel: Int = p1.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val batteryScale: Int = p1.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        val batteryStatus : Int = p1.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val chargePlug : Int = p1.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge : Boolean  = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge : Boolean  = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
        val batteryPercentage = (batteryLevel / batteryScale.toFloat() * 100).toInt()

        callback?.onBatteryStatusChanged(batteryPercentage, usbCharge, acCharge)
    }


}