package com.linksfield.lpa_example.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * CreateDate: 2020/8/26 17:14
 * Author: you
 * Description:
 */
@Dao
interface WifiDeviceDao {
    @Query("SELECT * FROM wifi_device ORDER BY id DESC")
    fun getDevices(): LiveData<List<WifiDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWifiDevice(device: WifiDevice)

    @Delete
    fun deleteWifiDevice(device: WifiDevice)
}