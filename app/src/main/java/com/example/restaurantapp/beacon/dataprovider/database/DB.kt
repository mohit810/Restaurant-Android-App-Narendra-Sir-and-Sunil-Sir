package com.example.restaurantapp.beacon.dataprovider.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.restaurantapp.beacon.dataprovider.database.CartItemEntity

@Database(entities = [CartItemEntity::class], version = 1)
abstract class DB: RoomDatabase() {
    abstract fun cartDAO():CartDAO
    companion object {
        private var dbinstance:DB? = null
        fun getDbInstance(context: Context):DB{
            if (dbinstance==null)
                dbinstance = Room.databaseBuilder<DB>(context,DB::class.java!!,"fight-covid-db").build()
            return dbinstance!!
        }
    }
}