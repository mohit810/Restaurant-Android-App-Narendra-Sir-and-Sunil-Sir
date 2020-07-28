package com.example.restaurantapp.beacon.booting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.restaurantapp.beacon.AdvertiserService


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            var serviceIntent : Intent = Intent(context, AdvertiserService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}