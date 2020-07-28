package com.example.restaurantapp.beacon.dataprovider.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.restaurantapp.dataprovider.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@Entity(tableName = "nearby_devices_info_table")
public class CartItemEntity {


    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    private int id;

    // This is not MAC Address. This is UNIQUE ID Assign to scanned device.
    @ColumnInfo(name = "bluetooth_mac_address")
    @SerializedName("bluetooth_mac_address")
    @Expose
    private String bluetoothMacAddress;

    @ColumnInfo(name = "device_name")
    @SerializedName("device_name")
    @Expose
    private String deviceName;

    // This is the RSSI of the scanned device.
    @ColumnInfo(name = "distance")
    @SerializedName("dist")
    @Expose
    private Integer distance;

    @ColumnInfo(name = "tx_power")
    @SerializedName("tx_power")
    @Expose
    private String txPower;

    @ColumnInfo(name = "tx_power_level")
    @SerializedName("tx_power_level")
    @Expose
    private String txPowerLevel;

    @ColumnInfo(name = "lat")
    @SerializedName("lat")
    @Expose
    private double latitude;

    @ColumnInfo(name = "long")
    @SerializedName("long")
    @Expose
    private double longitude;

    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    @SerializedName("ts")
    @Expose
    private Integer timeStamp;


    public CartItemEntity(String bluetoothMacAddress, Integer distance, String txPower, String txPowerLevel,String name) {
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.distance = distance;
        this.txPower = txPower;
        this.txPowerLevel = txPowerLevel;
        this.deviceName = name;
        timeStamp = Constants.Companion.getCurrentEpochTimeInSec();
    }
public CartItemEntity(){

}

    public String getBluetoothMacAddress() {
        return bluetoothMacAddress;
    }

    public void setBluetoothMacAddress(String bluetoothMacAddress) {
        this.bluetoothMacAddress = bluetoothMacAddress;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Integer timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTxPower() {
        return txPower;
    }

    public void setTxPower(String txPower) {
        this.txPower = txPower;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getTxPowerLevel() {
        return txPowerLevel;
    }

    public void setTxPowerLevel(String txPowerLevel) {
        this.txPowerLevel = txPowerLevel;
    }
}