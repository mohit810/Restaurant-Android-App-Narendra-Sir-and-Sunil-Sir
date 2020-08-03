package com.example.restaurantapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.restaurantapp.beacon.AdvertiserService
import com.example.restaurantapp.dataprovider.Constants
import com.example.restaurantapp.dataprovider.sharedpref.Prefs
import com.example.restaurantapp.locationwithlivedata.GpsUtils
import com.example.restaurantapp.locationwithlivedata.LocationViewModel
import com.example.restaurantapp.login.Login
import com.example.restaurantapp.register.Register
import com.example.restaurantapp.starting.config.AppPrefs
import com.example.restaurantapp.starting.view.activity.OnBoardingActivity
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File

class HomeActivity : AppCompatActivity() {
    private lateinit var locationViewModel: LocationViewModel
    var liveaddress: String? = null
    private var isGPSEnabled = false
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var advertisingFailureReceiver: BroadcastReceiver? = null
    /**
     * Lets user toggle BLE Advertising.
     */
    private var mSwitch: Switch? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if the app is launched before
        if (AppPrefs(this).isFirstTimeLaunch()) {
            startActivity(Intent(this, OnBoardingActivity::class.java))
            finish()
        }
        //status bar
        val window = this.window
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
// finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        setContentView(R.layout.activity_home)
        //toolbar(toolBar, resources.getString(R.string.home_screen_title))

        //checking the login status
        if (Prefs.checker.equals("on")) {
            home_login_btn.visibility = View.GONE
            home_Signup_btn.visibility = View.GONE
            startUp()
        } else {
            val wrapper = ContextWrapper(applicationContext)
            val mydir = wrapper.getDir("images", Context.MODE_PRIVATE)
            if (!mydir.exists()) {
                mydir.mkdirs()
            }
            val fileUri = File(mydir, "profilepic.png")
            Prefs.profilePicUri = fileUri.toString()
            startUp()
        }
        //new beacon addition for ns yadav sir and sunil sir
        checkBluetooth( savedInstanceState)
        advertisingFailureReceiver = object : BroadcastReceiver() {
            /**
             * Receives Advertising error codes from `AdvertiserService` and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                val errorCode =
                    intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1)
                var errorMessage: String = "start_error_prefix"
                when (errorCode) {
                    AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> errorMessage += " " + "start_error_already_started"

                    AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> errorMessage += " " + "start_error_too_large"

                    AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> errorMessage += " " + "start_error_unsupported"

                    AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> errorMessage += " " + "start_error_internal"

                    AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> errorMessage += " " + "start_error_too_many"

                    AdvertiserService.ADVERTISING_TIMED_OUT -> errorMessage =
                        " " + "advertising_timedout"
                    else -> errorMessage += " " + "start_error_unknown"
                }
                Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        mSwitch = advertise_switch as Switch
        /**
         * Called when switch is toggled - starts or stops advertising.
         */
        mSwitch!!.setOnClickListener {v:View ->
            // Is the toggle on?
            val on = (v as Switch).isChecked
            if (on) {
                startAdvertising()
            } else {
                stopAdvertising()
            }
        }
    }
    /**
     * When app goes off screen, unregister the Advertising failure Receiver to stop memory leaks.
     * (and because the app doesn't care if Advertising fails while the UI isn't active)
     */
    @SuppressLint("RestrictedApi")
    override fun onPause() {
        super.onPause()
        getActivity(this)!!.unregisterReceiver(advertisingFailureReceiver)
    }
    /**
     * When app comes on screen, check if BLE Advertisements are running, set switch accordingly,
     * and register the Receiver to be notified if Advertising fails.
     */
    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        if (AdvertiserService.running) {
            mSwitch!!.isChecked = true
        } else {
            mSwitch!!.isChecked = false
        }
        val failureFilter = IntentFilter(AdvertiserService.ADVERTISING_FAILED)
        getActivity(this)!!.registerReceiver(advertisingFailureReceiver, failureFilter)
    }
    /**
     * Starts BLE Advertising by starting `AdvertiserService`.
     */
    private fun startAdvertising() {
        val c: Context = this
        c.startService(getAdvertisingServiceIntent(c))
    }
    /**
     * Stops BLE Advertising by stopping `AdvertiserService`.
     */
    private fun stopAdvertising() {
        val c: Context = this
        c.stopService(getAdvertisingServiceIntent(c))
        mSwitch!!.isChecked = false
    }
    companion object {
        /**
         * Returns Intent addressed to the `AdvertiserService` class.
         */
        private fun getAdvertisingServiceIntent(c: Context): Intent {
            return Intent(c, AdvertiserService::class.java)
        }
    }

    private fun checkBluetooth(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            mBluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .adapter

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter!!.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter!!.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.

                    } else {

                        // Bluetooth Advertisements are not supported.
                        Toast.makeText(this,"bluetooth not supported",Toast.LENGTH_SHORT).show()
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT)
                }
            } else {

                // Bluetooth is not supported.
                Toast.makeText(this,"bluetooth not supported",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startUp() {
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        GpsUtils(this).turnGPSOn(object : GpsUtils.OnGpsListener {

            override fun gpsStatus(isGPSEnable: Boolean) {
                this@HomeActivity.isGPSEnabled = isGPSEnable
            }
        })
        home_menu_btn.setOnClickListener() {
            val intent = Intent(this, Base::class.java)
            var li = startLocationUpdate()
            Prefs.gpsAddress = liveaddress.toString()
//            intent.putExtra("Data", liveaddress)
            startActivity(intent)
        }
        home_Signup_btn.setOnClickListener() {
            startActivity(Intent(this, Register::class.java))
        }
        home_login_btn.setOnClickListener() {
            startActivity(Intent(this, Login::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                invokeLocationAction()
            }
        }
    }

    private fun invokeLocationAction() {
        when {
            isPermissionsGranted() -> startLocationUpdate()
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH, Manifest.permission.RECEIVE_BOOT_COMPLETED), LOCATION_REQUEST
            )
        }
    }

    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(this, androidx.lifecycle.Observer {
            liveaddress = it.addressgps
        }
        )
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                invokeLocationAction()
            }
        }
    }
}

const val GPS_REQUEST = 101
const val LOCATION_REQUEST = 100

