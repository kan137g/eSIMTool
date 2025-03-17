package com.linksfield.lpa_example.data.db

import com.linksfield.lpa_example.App

/**
 * CreateDate: 2020/8/26 17:18
 * Author: you
 * Description:
 */
class WifiDeviceRepository private constructor(
        private val wifiDeviceDao: WifiDeviceDao
) {

    fun getWifiDevices() = wifiDeviceDao.getDevices()

    suspend fun addWifiDevice(wifiDevice: WifiDevice) {
        wifiDeviceDao.insertWifiDevice(wifiDevice)
    }

    suspend fun removeWifiDevice(wifiDevice: WifiDevice) {
        wifiDeviceDao.deleteWifiDevice(wifiDevice)
    }

    companion object {

        @Volatile
        private var instance: WifiDeviceRepository? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: WifiDeviceRepository(
                            AppDatabase.getInstance(App.INSTANCE).wifiDeviceDao()).also { instance = it }
                }
    }

}