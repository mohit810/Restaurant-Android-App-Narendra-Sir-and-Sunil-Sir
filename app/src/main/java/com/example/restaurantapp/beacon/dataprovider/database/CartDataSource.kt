package com.example.restaurantapp.beacon.dataprovider.database

import com.example.restaurantapp.beacon.dataprovider.database.CartItemEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface CartDataSource {

    fun insertNearbyUser(bluetoothData: CartItemEntity?): Long

    fun insertAllNearbyUsers(bluetoothData: List<CartItemEntity?>?): List<Long?>?

    fun getAllNearbyDevices(): Flowable<List<CartItemEntity?>?>

    fun getRowCount(): Long


    fun getFirstXNearbyDeviceInfo(limit: Int): List<CartItemEntity?>?

    fun getXNearbyDeviceInfoWithOffset(
        limit: Int,
        offset: Int
    ): List<CartItemEntity?>?

     fun deleteXDaysOldData(daysTimeStamp: Int, currentTimeStamp: Int): Int

    fun deleteAll(bluetoothData: List<CartItemEntity?>?)
}