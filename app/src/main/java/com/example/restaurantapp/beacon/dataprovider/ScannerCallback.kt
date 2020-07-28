package com.example.restaurantapp.beacon.dataprovider

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.restaurantapp.SafEat
import com.example.restaurantapp.beacon.dataprovider.database.CartItemEntity
import com.example.restaurantapp.beacon.dataprovider.database.DBManager
import com.example.restaurantapp.dataprovider.Constants

object ScannerCallback : ScanCallback() {

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
//                mAdapter!!.add(result)
            }
//            mAdapter!!.notifyDataSetChanged()
        }

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)
            if (result == null || result.device == null || result.device.name == null)
                return

             if (result.rssi > -78) {/* use this for approximation of 1 meter*/

                   Constants.vibratePhone()
                /*Toast.makeText(BLEApp.getInstance(), result.toString(), Toast.LENGTH_SHORT).show()*/
                   Log.d("yoo",result.rssi.toString())
                   var txPower: String = Constants.EMPTY
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                       txPower = result.txPower.toString()
                   }
                   var txPowerLevel = ""
                   if (result.scanRecord != null) {
                       txPowerLevel = result.scanRecord!!.txPowerLevel.toString()
                   }
                   val bluetoothModel = BluetoothModel(
                       result.device.name,
                       result.device.toString(), result.rssi, txPower, txPowerLevel
                   )
                   storeDetectedUserDeviceInDB(bluetoothModel)
                }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(SafEat.getInstance(), "Scan failed with error: $errorCode", Toast.LENGTH_LONG)
                .show()
        }

    /**
     * This method will store the detected device info into the local database to query in future if the need arise
     * to push the data
     *
     * @param bluetoothModel The newly detected device nearby
     */
    fun storeDetectedUserDeviceInDB(bluetoothModel: BluetoothModel?) {
        if (bluetoothModel != null) {
            val bluetoothData = CartItemEntity(
                bluetoothModel.address, bluetoothModel.rssi,
                bluetoothModel.txPower, bluetoothModel.txPowerLevel,
                bluetoothModel.name
            )
            val loc: Location? =
                SafEat.getAppLastLocation()
            if (loc != null) {
                bluetoothData.setLatitude(loc.latitude)
                bluetoothData.setLongitude(loc.longitude)
            }
            /*Toast.makeText(BLEApp.getInstance(),"your position is :- longitude "+ bluetoothData.longitude.toString()+" and latitude is "+ bluetoothData.latitude.toString() , Toast.LENGTH_SHORT).show()
            Log.d("yoo", bluetoothData.longitude.toString())
            Log.d("yoo", bluetoothData.latitude.toString())*/
            //can call api for uploading the data to server here
            DBManager.insertNearbyDetectedDeviceInfo(bluetoothData)
            /*Toast.makeText(BLEApp.getInstance(),"the devices are " + CartDatabase.getInstance().cartDAO().getAllNearbyDevices() , Toast.LENGTH_SHORT).show()*/ // new addition post submission
        }

    }

}