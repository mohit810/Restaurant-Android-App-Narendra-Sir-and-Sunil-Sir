package com.example.restaurantapp.beacon.dataprovider.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

public class DBManager {


    private static int numCores = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static Task<List<Long>> insertNearbyDetectedDeviceInfo(List<CartItemEntity> userDeviceInfoList) {

        return Tasks.call(executor, () -> CartDatabase.getInstance().cartDAO().insertAllNearbyUsers(userDeviceInfoList));
    }

    public static Task<Long> insertNearbyDetectedDeviceInfo(CartItemEntity userDeviceInfo) {

        return Tasks.call(executor, () -> CartDatabase.getInstance().cartDAO().insertNearbyUser(userDeviceInfo));
    }
}