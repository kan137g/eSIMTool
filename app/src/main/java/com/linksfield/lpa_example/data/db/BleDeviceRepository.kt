package com.linksfield.lpa_example.data.db

import com.linksfield.lpa_example.App

/**
 * CreateDate: 2020/8/14 17:13
 * Author: you
 * Description:
 */
class BleDeviceRepository private constructor(
        private val bleDeviceDao: BleDeviceDao
) {

    fun getBleDevices() = bleDeviceDao.getDevices()

    suspend fun addBleDevice(bleDevice: BleDevice) {
        bleDeviceDao.insertBleDevice(bleDevice)
    }

    suspend fun removeBleDevice(bleDevice: BleDevice) {
        bleDeviceDao.deleteBleDevice(bleDevice)
    }

    companion object {

        @Volatile
        private var instance: BleDeviceRepository? = null

//        fun getInstance(deviceDao: BleDeviceDao) =
//                instance ?: synchronized(this) {
//                    instance ?: BleDeviceRepository(deviceDao).also { instance = it }
//                }
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: BleDeviceRepository(
                            AppDatabase.getInstance(App.INSTANCE).bleDeviceDao()).also { instance = it }
                }
    }

}