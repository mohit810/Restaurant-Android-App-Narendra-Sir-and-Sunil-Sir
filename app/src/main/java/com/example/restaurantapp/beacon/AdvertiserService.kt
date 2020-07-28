package com.example.restaurantapp.beacon

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.restaurantapp.HomeActivity
import com.example.restaurantapp.R
import com.example.restaurantapp.SafEat
import com.example.restaurantapp.beacon.dataprovider.ScannerCallback
import com.example.restaurantapp.dataprovider.Constants
import java.util.*
import java.util.concurrent.TimeUnit

internal class AdvertiserService: Service(){
    private var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var mAdvertiseCallback: AdvertiseCallback? = null
    private var mHandler: Handler? = null
    private var timeoutRunnable: Runnable? = null
    private var timer: Timer? = null
    private val FIVE_MINUTES = 5 * 60 * 1000
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanCallback: ScanCallback? = null
    /**
     * Length of time to allow advertising before automatically shutting off. (10 minutes)
     */
    private val TIMEOUT = TimeUnit.MILLISECONDS.convert(
        10,
        TimeUnit.MINUTES
    )

    override fun onCreate() {
        running = true
        initialize()
//        startAdvertising()
        advertiseAndScan()
        setTimeout()
        super.onCreate()
    }

    override fun onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        running = false
        stopAdvertising()
        mHandler!!.removeCallbacks(timeoutRunnable)
        stopForeground(true)
        super.onDestroy()
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private fun initialize() {
        if (mBluetoothLeAdvertiser == null) {
            val mBluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager != null) {
                val mBluetoothAdapter = mBluetoothManager.adapter
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
                } else {
                    Toast.makeText(this, "bt_null", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "bt_null", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private fun setTimeout() {
        mHandler = Handler()
        timeoutRunnable = Runnable {
            Log.d(
                TAG,
                "AdvertiserService has reached timeout of $TIMEOUT milliseconds, stopping advertising."
            )
            sendFailureIntent(ADVERTISING_TIMED_OUT)
            stopSelf()
        }
        mHandler!!.postDelayed(timeoutRunnable, TIMEOUT)
    }
    //new addition code post 1st run
    private fun advertiseAndScan() {
        if (timer != null) {
            timer!!.cancel()
        }
        timer = Timer()
        timer!!.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    // if (isBluetoothAvailable()) { //for safe run add this
                    startAdvertising()
                    discover()
                    // }
                }
            },
            0,
            FIVE_MINUTES.toLong()
        )

    }
    /**
     * Start scanning BLE devices with provided scan mode
     * @param scanMode
     */
    private fun discover(/*scanMode: Int*/) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        mBluetoothLeScanner = adapter.bluetoothLeScanner
        if (mBluetoothLeScanner == null) {
            return
        }
        val filters: MutableList<ScanFilter> = ArrayList()
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(Constants.Service_UUID.toString())))
            .build()
        filters.add(filter)
        val settings = ScanSettings.Builder()
//            .setScanMode(scanMode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settings.setMatchMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setLegacy(false)
            settings.setPhy(BluetoothDevice.PHY_LE_1M)
        }
        try {
            if (mScanCallback == null) {
                Log.d(TAG, "Starting Scanning")

                // Will stop the scanning after a set time.
                mHandler!!.postDelayed({ stopScanning() }, SCAN_PERIOD)

                // Kick off a new scan.
                mScanCallback = ScannerCallback
                mBluetoothLeScanner!!.startScan(filters, settings.build(), ScannerCallback)
                val toastText: String = ("scan_start_toast "
                        + TimeUnit.SECONDS.convert(
                    SCAN_PERIOD,
                    TimeUnit.MILLISECONDS
                ) + " "
                        + "seconds")
                Toast.makeText(SafEat.getInstance(), toastText, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(SafEat.getInstance(), "Already scanning", Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            //Handle Android internal exception for BT adapter not turned ON(Known Android bug)
            Log.d(TAG, "error = " + ex)
        }
    }
    fun stopScanning() {
        Log.d(TAG, "Stopping Scanning")

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner!!.stopScan(ScannerCallback)
        mScanCallback == null

        // Even if no new results, update 'last seen' times.
//        mAdapter!!.notifyDataSetChanged()
    }
    /**
     * Starts BLE Advertising.
     */
    private fun startAdvertising() {
        goForeground()
        Log.d(TAG, "Service: Starting Advertising")
        if (mAdvertiseCallback == null) {
            val settings = buildAdvertiseSettings()
            val data = buildAdvertiseData()
            mAdvertiseCallback = SampleAdvertiseCallback()
            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser!!.startAdvertising(
                    settings, data,
                    mAdvertiseCallback
                )
            }
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     *
     * Callers should call stopForeground(true) when background work is complete.
     */
    private fun goForeground() {
        createNotificationChannel()
        val notificationIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )
        val n: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .build()
        }
        startForeground(FOREGROUND_NOTIFICATION_ID, n)
    }

    /**
     * Stops BLE Advertising.
     */
    private fun stopAdvertising() {
        Log.d(TAG, "Service: Stopping Advertising")
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser!!.stopAdvertising(mAdvertiseCallback)
            mAdvertiseCallback = null
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private fun buildAdvertiseData(): AdvertiseData {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         * This includes everything put into AdvertiseData including UUIDs, device info, &
         * arbitrary service or manufacturer data.
         * Attempting to send packets over this limit will result in a failure with error code
         * AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         * onStartFailure() method of an AdvertiseCallback implementation.
         */
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.addServiceUuid(Constants.Service_UUID)
        dataBuilder.setIncludeDeviceName(true)

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());
        return dataBuilder.build()
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private fun buildAdvertiseSettings(): AdvertiseSettings {
        val settingsBuilder = AdvertiseSettings.Builder()
//        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            .setTimeout(0)
        return settingsBuilder.build()
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private inner class SampleAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.d(TAG, "Advertising failed")
            sendFailureIntent(errorCode)
            stopSelf()
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the `AdvertiserFragment`.
     */
    private fun sendFailureIntent(errorCode: Int) {
        val failureIntent = Intent()
        failureIntent.action = ADVERTISING_FAILED
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode)
        sendBroadcast(failureIntent)
    }

    companion object {
        private val TAG = AdvertiserService::class.java.simpleName
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val SCAN_PERIOD: Long = 500000
        /**
         * A global variable to let AdvertiserFragment check if the Service is running without needing
         * to start or bind to it.
         * This is the best practice method as defined here:
         * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
         */
        var running = false
        const val ADVERTISING_FAILED =
            "com.example.android.bluetoothadvertisements.advertising_failed"
        const val ADVERTISING_FAILED_EXTRA_CODE = "failureCode"
        const val ADVERTISING_TIMED_OUT = 6


        private val CHANNEL_ID = "ForegroundService Kotlin"
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, AdvertiserService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, AdvertiserService::class.java)
            context.stopService(stopIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}