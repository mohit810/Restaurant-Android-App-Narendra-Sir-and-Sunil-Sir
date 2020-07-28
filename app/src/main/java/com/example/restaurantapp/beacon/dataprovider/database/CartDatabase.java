package com.example.restaurantapp.beacon.dataprovider.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.restaurantapp.SafEat;

@Database(entities = {CartItemEntity.class}, version = 1)
public abstract class CartDatabase extends RoomDatabase {
    private static CartDatabase sInstance;

    private static final String DATABASE_NAME = "fight-covid-db";

    public static CartDatabase getInstance() {
        Context context = SafEat.Companion.getInstance().getApplicationContext();
        if (sInstance == null) {
            sInstance = buildDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static CartDatabase buildDatabase(final Context appContext) {//}, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, CartDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }
                })
                .build();
    }
public abstract CartDAO cartDAO();
}
