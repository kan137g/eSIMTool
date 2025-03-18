package com.linksfield.lpa_example.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linksfield.lpa_example.data.db.BleDevice
import com.linksfield.lpa_example.data.db.BleDeviceRepository
import com.linksfield.lpa_example.data.db.WifiDevice
import com.linksfield.lpa_example.data.db.WifiDeviceRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val bleDeviceRepository: BleDeviceRepository, private val wifiDeviceRepository: WifiDeviceRepository) : ViewModel() {


    val bleDevices: LiveData<List<BleDevice>> = bleDeviceRepository.getBleDevices()

    val wifiDevices: LiveData<List<WifiDevice>> = wifiDeviceRepository.getWifiDevices()

    fun addBleDevice(bleDevice: BleDevice) {
        viewModelScope.launch {
            bleDeviceRepository.addBleDevice(bleDevice)
        }
    }

    fun removeBleDevice(bleDevice: BleDevice) {
        viewModelScope.launch {
            bleDeviceRepository.removeBleDevice(bleDevice)
        }
    }

    fun addWifiDevice(wifiDevice: WifiDevice) {
        viewModelScope.launch {
            wifiDeviceRepository.addWifiDevice(wifiDevice)
        }
    }

    fun removeWifiDevice(wifiDevice: WifiDevice) {
        viewModelScope.launch {
            wifiDeviceRepository.removeWifiDevice(wifiDevice)
        }
    }

    companion object {

        @Volatile
        private var instance: HomeViewModel? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance
                            ?: HomeViewModel(BleDeviceRepository.getInstance(), WifiDeviceRepository.getInstance()).also { instance = it }
                }
    }

}