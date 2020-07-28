package com.example.restaurantapp.beacon.dataprovider.database


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.restaurantapp.beacon.dataprovider.database.CartItemEntity
import io.reactivex.Flowable
import java.util.*

@Dao
interface CartDAO {
    @Insert
    fun insertNearbyUser(bluetoothData: CartItemEntity?): Long

    @Insert
    fun insertAllNearbyUsers(bluetoothData: List<CartItemEntity?>?): List<Long?>?

    @Query("SELECT * FROM nearby_devices_info_table")
    fun getAllNearbyDevices(): Flowable<List<CartItemEntity?>?>

    @Query("SELECT COUNT(*) FROM nearby_devices_info_table")
    fun getRowCount(): Long

    @Query("SELECT * FROM nearby_devices_info_table LIMIT :limit")
    fun getFirstXNearbyDeviceInfo(limit: Int): List<CartItemEntity?>?

    @Query("SELECT * FROM nearby_devices_info_table  LIMIT :limit OFFSET :offset")
    fun getXNearbyDeviceInfoWithOffset(
        limit: Int,
        offset: Int
    ): List<CartItemEntity?>?

    @Query("DELETE FROM nearby_devices_info_table WHERE (:currentTimeStamp - timestamp) >= :daysTimeStamp")
    fun deleteXDaysOldData(daysTimeStamp: Int, currentTimeStamp: Int): Int

    @Delete
    fun deleteAll(bluetoothData: List<CartItemEntity?>?)
}