package com.example.restaurantapp.dataprovider

import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.restaurantapp.SafEat
import java.util.*

/*
* Added for ns yadav sir and sunil sir
* */

class Constants {
    companion object {
        fun vibratePhone(){ // new addition post submission
            val v: Vibrator = SafEat.getInstance().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 100 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //deprecated in API 26
                @Suppress("DEPRECATION")
                v.vibrate(100)
            }
        }
        fun getCurrentEpochTimeInSec(): Int {
            val dateObj = Date()
            return (dateObj.time / 1000).toInt()
        }

        val EMPTY =""
        val REQUEST_ENABLE_BT = 1
        val Service_UUID = ParcelUuid
            .fromString( "0000b81d-0000-1000-8000-00805f9b34fb")
    }
}