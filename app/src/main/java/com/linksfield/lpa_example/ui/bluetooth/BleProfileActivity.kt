package com.linksfield.lpa_example.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.linksfield.lpa_example.App
import com.linksfield.lpa_example.base.BaseProfileActivity
import com.linksfield.lpa_example.data.db.BleDevice
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpad.device.EuiccBleDevice
import com.linksfield.lpad.grpc.LPAdClient
import kotlinx.android.synthetic.main.app_bar_main.toolbar
import kotlinx.android.synthetic.main.recyclerview.refreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CreateDate: 2020/8/19 11:49
 * Author: you
 * Description:
 */
class BleProfileActivity : BaseProfileActivity<ActivityCommonBinding>() {

    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null

    companion object {
        val TAG = BleProfileActivity::class.java.name
        const val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        const val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
    }

    override fun initViews() {
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)
        if (mDeviceAddress == null) {
            return
        }

        super.initViews()
        toolbar.title = mDeviceName
    }

    override fun checkConnectStateAndUpdateProfileView() {
        if (device != null && (device as EuiccBleDevice).isConnected()) {
            lifecycleScope.launch { updateProfileView() }
            return
        }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        device = EuiccBleDevice(this@BleProfileActivity, bluetoothAdapter, mDeviceAddress!!).apply {
            //NOTE: lib 1.0.4添加设置蓝牙传输间隔时间的方法，单位为ms,支持最大可设置为1000ms
            setIntervalTime(20)
            //设置蓝牙传输协议的数据长度占用字节数，默认2字节，仅支持2和4字节。
            setProtocolLength(2)
        }
        listenConnectState()
        lifecycleScope.launch(Dispatchers.IO) {
            if ((device as EuiccBleDevice).open()) {
                bleDevice = (device as EuiccBleDevice).getBleMac()
                    ?.let { BleDevice(name = mDeviceName, address = it) }
                val deviceInfo = device?.deviceInfo
                val imei = deviceInfo?.imei
                if (imei.isNullOrEmpty()) {
                    device?.close()
                    withContext(Dispatchers.Main) {
                        refreshLayout.finishRefresh(false)
                    }
                    return@launch
                } else {
                    try {
                        val lpAdClient = LPAdClient.createLPAdClient(
                            this@BleProfileActivity,
                            device as EuiccBleDevice
                        )
                        App.INSTANCE.setLPAdClient(lpAdClient)
                        updateProfileView()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, e.toString())
                            e.message?.let { showToast(it) }
                            refreshLayout.finishRefresh(false)
                        }
                    }
                }
            }
        }
    }

    override fun setConnectType() {
        type = ConnectType.BLE
    }

}