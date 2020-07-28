package com.example.restaurantapp

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.example.restaurantapp.dataprovider.sharedpref.Prefs

class SafEat:Application() {
    /*
    * added for ns yadav sir and sunil sir
    * */
    init {
        instance = this
    }

    companion object {
        private var instance: SafEat? = null
        var lastKnownLocation: Location? = null
        fun getAppLastLocation(): Location? {
            getDeviceLastKnownLocation()
            return lastKnownLocation
        }

        fun getDeviceLastKnownLocation(): Location? {
//        if (CorUtility.Companion.isLocationPermissionAvailable(BLEApp.getInstance())) {
            val mLocationManager =
                getInstance().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers =
                mLocationManager.getProviders(true)
            for (provider in providers) {
                try {
                    val l =
                        mLocationManager.getLastKnownLocation(provider) ?: continue
                    if (lastKnownLocation == null || l.accuracy > lastKnownLocation!!.getAccuracy()) {
                        lastKnownLocation = l
                    }
                } catch (e: SecurityException) {
                }
            }
//        }
            return lastKnownLocation
        }
        fun getInstance(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        Prefs.init(applicationContext) // older code
        super.onCreate()
        val context: Context = SafEat.getInstance()// new addidtion for ns yadav sir and sunil sir
    }

}