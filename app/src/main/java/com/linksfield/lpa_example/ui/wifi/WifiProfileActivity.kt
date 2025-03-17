package com.linksfield.lpa_example.ui.wifi

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.linksfield.lpa_example.App
import com.linksfield.lpa_example.base.BaseProfileActivity
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpad.device.EuiccWifiDevice
import com.linksfield.lpad.grpc.LPAdClient
import kotlinx.android.synthetic.main.recyclerview.refreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CreateDate: 2020/8/25 17:40
 * Author: you
 * Description:
 */
class WifiProfileActivity : BaseProfileActivity<ActivityCommonBinding>() {

    override fun checkConnectStateAndUpdateProfileView() {
        lifecycleScope.launch(Dispatchers.IO) {
            if (device != null && (device as EuiccWifiDevice).isConnected) {
                updateProfileView()
                return@launch
            }

            device = EuiccWifiDevice(option.ip, option.port)
            listenConnectState()
            val deviceInfo = device?.deviceInfo
            if (deviceInfo?.imei.isNullOrEmpty()) {
                device?.close()
                withContext(Dispatchers.Main) {
                    refreshLayout.finishRefresh(false)
                }
            } else {
                try {
                    val lpadClient =
                        device?.let { LPAdClient.createLPAdClient(this@WifiProfileActivity, it) }
                    App.INSTANCE.setLPAdClient(lpadClient)
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

    override fun setConnectType() {
        type = ConnectType.WIFI
    }

}