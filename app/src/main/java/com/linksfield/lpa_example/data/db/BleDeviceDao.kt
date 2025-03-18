package com.linksfield.lpa_example.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.linksfield.lpa_example.data.db.BleDevice

/**
 * CreateDate: 2020/8/12 18:27
 * Author: you
 * Description:
 */
@Dao
interface BleDeviceDao {

    @Query("SELECT * FROM ble_device ORDER BY id DESC")
    fun getDevices(): LiveData<List<BleDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBleDevice(device: BleDevice)

    @Delete
    fun deleteBleDevice(device: BleDevice)
}