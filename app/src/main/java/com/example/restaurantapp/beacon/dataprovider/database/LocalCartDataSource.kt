package com.example.restaurantapp.beacon.dataprovider.database

import com.example.restaurantapp.beacon.dataprovider.database.CartItemEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDataSource(private val cartDAO:CartDAO):CartDataSource {
    override fun insertNearbyUser(bluetoothData: CartItemEntity?): Long {
        return cartDAO.insertNearbyUser(bluetoothData)
    }

    override fun insertAllNearbyUsers(bluetoothData: List<CartItemEntity?>?): List<Long?>? {
        return cartDAO.insertAllNearbyUsers(bluetoothData)
    }

    override fun getAllNearbyDevices(): Flowable<List<CartItemEntity?>?> {
       return cartDAO.getAllNearbyDevices()
    }

    override fun getRowCount(): Long {
        return cartDAO.getRowCount()
    }

    override fun getFirstXNearbyDeviceInfo(limit: Int): List<CartItemEntity?>? {
       return cartDAO.getFirstXNearbyDeviceInfo(limit)
    }

    override fun getXNearbyDeviceInfoWithOffset(limit: Int, offset: Int): List<CartItemEntity?>? {
        return cartDAO.getXNearbyDeviceInfoWithOffset(limit,offset)
    }

    override fun deleteXDaysOldData(daysTimeStamp: Int, currentTimeStamp: Int): Int {
        return cartDAO.deleteXDaysOldData(daysTimeStamp, currentTimeStamp)
    }

    override fun deleteAll(bluetoothData: List<CartItemEntity?>?) {
        return cartDAO.deleteAll(bluetoothData)
    }

}