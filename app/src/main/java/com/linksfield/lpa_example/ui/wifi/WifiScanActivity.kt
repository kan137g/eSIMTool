package com.linksfield.lpa_example.ui.wifi

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.linksfield.lpa_example.base.BaseActivity
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.DEVICE_NAME
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.IP_ADDRESS
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.PORT
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpad.utils.CommonUtils
import com.linksfield.lpad.utils.WIFI_SCAN_PERIOD
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.activity_common.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * CreateDate: 2020/9/8 13:59
 * Author: you
 * Description:
 */
class WifiScanActivity : BaseActivity<ActivityCommonBinding>() {

    private val TAG = WifiScanActivity::class.java.name
    private lateinit var mNsdManager: NsdManager
    private lateinit var mAdapter: WifiScanAdapter
    private lateinit var job: Job
    private var wifiInfo: Pair<String?, String?>? = null

    override fun initViews() {
        if (!isWifiConnected()) {
            showToast("please connect wifi!")
            return
        }
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 1000)
        } else {
            wifiInfo = getGatewayIPAddress()
        }
        mNsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        fab.setOnClickListener {
            val intent = Intent(this, WifiProfileActivity::class.java)
            var ipEt: TextInputEditText? = null
            var portEt: TextInputEditText? = null
            val pop = XPopup.Builder(this)
                .autoOpenSoftInput(true)
                .isDestroyOnDismiss(true)
                .asConfirm(
                    "Please input IP Address and port", "",
                    getString(R.string.xpopup_cancel), getString(R.string.xpopup_ok),
                    {
                        val ip = ipEt?.text.toString()
                        val port = portEt?.text.toString()
                        if (TextUtils.isEmpty(ip)) {
                            showToast("please input IP Address")
                            return@asConfirm
                        }
                        if (TextUtils.isEmpty(port)) {
                            showToast("please input port")
                            return@asConfirm
                        }

                        intent.putExtra(IP_ADDRESS, ip)
                        intent.putExtra(PORT, port)
                        startActivity(intent)
                    }, null, false, R.layout.popup_wifi
                )
                .show()
            ipEt = pop.findViewById(R.id.ip)
            val ipAddress = wifiInfo?.second ?: ""
            ipEt.text = Editable.Factory.getInstance().newEditable(ipAddress)
            ipEt.requestFocus()
            ipEt.setSelection(ipAddress.length)
            portEt = pop.findViewById(R.id.port)
        }
        toolbar.title = "Wifi"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        mAdapter = WifiScanAdapter(mutableListOf())
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        mAdapter.setOnItemChildClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            if (item.itemType == ItemEntity.TYPE_NSD) {
                val serviceInfo = item.info!!
                val intent = Intent(this, WifiNSDActivity::class.java)
                intent.putExtra(DEVICE_NAME, CommonUtils.getRealName(serviceInfo.serviceName))
                intent.putExtra(IP_ADDRESS, serviceInfo.host.hostAddress)
                intent.putExtra(PORT, serviceInfo.port.toString())
                startActivity(intent)
            } else {
                val entity = item.wifiEntity!!
                val intent = Intent(this, WifiProfileActivity::class.java)
                intent.putExtra(DEVICE_NAME, entity.name)
                intent.putExtra(IP_ADDRESS, entity.host)
                intent.putExtra(PORT, entity.port)
                startActivity(intent)
            }
        }

        job = Job()
        val coroutineScope = CoroutineScope(job)

        refreshLayout.setOnRefreshListener {
            coroutineScope.launch {
                delay(WIFI_SCAN_PERIOD)
                withContext(Dispatchers.Main) {
                    try {
                        mNsdManager.stopServiceDiscovery(mDiscoveryListener)
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }
                }
            }
            scanWifi()
        }
        if(wifiInfo != null) {
            refreshLayout.autoRefresh()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    wifiInfo = getGatewayIPAddress()
                    if(wifiInfo != null) {
                        refreshLayout.autoRefresh()
                    }
                }
            }
        }
    }

    private fun scanWifi() {
        mAdapter.setEmptyView(R.layout.view_empty)
        mAdapter.setNewInstance(mutableListOf())
        mAdapter.addData(
            ItemEntity(
                ItemEntity.TYPE_WIFI,
                wifiEntity = WifiEntity(wifiInfo?.first?.replace("\"", "") ?: "", wifiInfo?.second ?: "", "12410")
            )
        )
        mNsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
    }

    private val mDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStartDiscoveryFailed: errorCode = $errorCode")
            refreshLayout.finishRefresh(false)
            showToast("failed to start discovery, errorCode is $errorCode")
            mNsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed: errorCode = $errorCode")
            refreshLayout.finishRefresh(false)
            mNsdManager.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStarted $serviceType")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStopped $serviceType")
            refreshLayout.finishRefresh(true)
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceFound $serviceInfo")
            serviceInfo?.let {
                if (serviceInfo.serviceName.contains("eSIM") && serviceInfo.serviceType == "_http._tcp.") {
                    mNsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {

                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.e(TAG, "onResolveFailed")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                            Log.i(TAG, "onServiceResolved")
                            runOnUiThread {
                                serviceInfo?.let {
                                    mAdapter.addData(
                                        ItemEntity(
                                            ItemEntity.TYPE_NSD,
                                            info = serviceInfo
                                        )
                                    )
                                }
                            }
                        }
                    })
                }
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.e(TAG, "onServiceLost")
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
        try {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun isWifiConnected(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.type == ConnectivityManager.TYPE_WIFI
    }

    @RequiresPermission(ACCESS_WIFI_STATE)
    private fun getGatewayIPAddress(): Pair<String?, String?> {
        val wifiManager =
            applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo
        val gatewayIPInt = dhcpInfo?.gateway
        val s = gatewayIPInt?.let { formatIPAddress(it) }
        return wifiManager.connectionInfo.ssid to s
    }

    private fun formatIPAddress(intIP: Int): String =
        String.format(
            "%d.%d.%d.%d",
            intIP and 0xFF,
            intIP shr 8 and 0xFF,
            intIP shr 16 and 0xFF,
            intIP shr 24 and 0xFF
        )
}