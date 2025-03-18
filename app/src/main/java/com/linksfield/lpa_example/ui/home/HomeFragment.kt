package com.linksfield.lpa_example.ui.home

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseFragment
import com.linksfield.lpa_example.databinding.FragmentHomeBinding
import com.linksfield.lpa_example.ui.bluetooth.BleProfileActivity
import com.linksfield.lpad.utils.BLE_SCAN_PERIOD
import com.linksfield.lpad.utils.WIFI_SCAN_PERIOD
import com.linksfield.lpad.utils.filterUuid
import com.linksfield.lpad.utils.isSavedDevice
import kotlinx.android.synthetic.main.fragment_home.ble_recyclerview
import kotlinx.android.synthetic.main.fragment_home.wifi_recyclerview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    companion object {
        const val REQUEST_CODE_ENABLE_BT = 0
        val TAG = HomeFragment::class.java.name
    }

    private lateinit var bleAdapter: BleAdapter
    private lateinit var wifiAdapter: WIFIAdapter
    private lateinit var homeViewModel: HomeViewModel
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private lateinit var mNsdManager: NsdManager

    private lateinit var job: Job
    private val scanList = mutableListOf<ScanResult>()
    private var isNsdStart: Boolean = false

    override fun initViews() {
        homeViewModel = HomeViewModel.getInstance()
        mNsdManager = requireContext().getSystemService(Context.NSD_SERVICE) as NsdManager
        bleAdapter = BleAdapter(arrayListOf())
        wifiAdapter = WIFIAdapter(arrayListOf())
        ble_recyclerview?.let {
            it.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = bleAdapter
        }
        wifi_recyclerview?.let {
            it.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = wifiAdapter
        }
        bleAdapter.setOnItemLongClickListener { _, _, position ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete saved device?")
                .setPositiveButton("Delete") { dialog, _ ->
                    homeViewModel.removeBleDevice(bleAdapter.getItem(position))
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            false
        }

        wifiAdapter.setOnItemLongClickListener { _, _, position ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete saved device?")
                .setPositiveButton("Delete") { dialog, _ ->
                    homeViewModel.removeWifiDevice(wifiAdapter.getItem(position))
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            false
        }

        homeViewModel.bleDevices.observe(viewLifecycleOwner) { devices ->
            bleAdapter.setNewInstance(devices.toMutableList())
            if (devices.isNotEmpty()) {
                scanList.clear()
                scanBle()
            }
        }
        homeViewModel.wifiDevices.observe(viewLifecycleOwner) { devices ->
            wifiAdapter.setNewInstance(devices.toMutableList())
            if (devices.isNotEmpty()) {
                scanWifi()
            }
        }

        bleAdapter.setOnItemChildClickListener { _, _, position ->
            val bleDevice = bleAdapter.getItem(position)
            val intent = Intent(context, BleProfileActivity::class.java)
            intent.putExtra(BleProfileActivity.EXTRAS_DEVICE_NAME, bleDevice.name)
            intent.putExtra(BleProfileActivity.EXTRAS_DEVICE_ADDRESS, bleDevice.temporaryAddress)
            startActivity(intent)
        }
    }

    private fun scanWifi() {
        job = Job()
        val coroutineScope = CoroutineScope(job)
        coroutineScope.launch {
            delay(WIFI_SCAN_PERIOD)
            stopServiceDiscovery()
        }
        Log.e(TAG, "nsdManager discoverServices")
        try {
            mNsdManager.discoverServices(
                "_http._tcp.",
                NsdManager.PROTOCOL_DNS_SD,
                mDiscoveryListener
            )
            isNsdStart = true
        } catch (e: Exception) {
            Log.e(TAG, "discoverServices error: ${e.message}")
        }
    }

    private fun stopServiceDiscovery() {
        if (isNsdStart) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener)
                isNsdStart = false
                Log.d(TAG, "Service discovery stopped")
            } catch (e: Exception) {
                Log.e(TAG, "nsdManager stop error: ${e.message}")
            }
        }
    }

    private val mDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStartDiscoveryFailed: errorCode = $errorCode")
            showToast("failed to start discovery, errorCode is $errorCode")
            mNsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed: errorCode = $errorCode")
            mNsdManager.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStarted")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStopped")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceFound")
            serviceInfo?.let {
                if (serviceInfo.serviceName.contains("eSIM") && serviceInfo.serviceType == "_http._tcp.") {
                    mNsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {

                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.e(TAG, "onResolveFailed")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                            Log.i(TAG, "onServiceResolved")
                            activity?.runOnUiThread {
                                val ip = "${serviceInfo?.host?.hostAddress}:${serviceInfo?.port}"
                                val wifiDevices = wifiAdapter.data
                                for (device in wifiDevices) {
                                    if (device.ip == ip) {
                                        device.canScan = true
                                        if (!wifi_recyclerview.isComputingLayout) {
                                            wifiAdapter.notifyItemChanged(
                                                wifiAdapter.getItemPosition(
                                                    device
                                                )
                                            )
                                        } else {
                                            runBlocking {
                                                delay(600L)
                                                activity?.runOnUiThread { wifiAdapter.notifyDataSetChanged() }
                                            }
                                        }
                                    }
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

    private fun startScan() {
        //SCAN_MODE_LOW_POWER--------耗电最少，扫描时间间隔最短
        //SCAN_MODE_BALANCED---------平衡模式，耗电适中，扫描时间间隔一般，我使用这种模式来更新设备状态
        //SCAN_MODE_LOW_LATENCY---------最耗电，扫描延迟时间短，打开扫描需要立马返回结果可以使用
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //前台扫描
            .build()
        val scanFilter = ScanFilter.Builder()
//                .setServiceUuid(ParcelUuid.fromString(EuiccGattAttributes.EUICC_SERVICE))
            .build()

        GlobalScope.launch {
            delay(BLE_SCAN_PERIOD)
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(mScanCallback)
            } catch (e: Exception) {
                Log.e(TAG, "bluetoothLeScanner stop error: ${e.message}")
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(
            mutableListOf(scanFilter),
            scanSettings,
            mScanCallback
        )
    }

    private val mScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { it ->
                if (result.filterUuid() && !scanList.any { it1 -> it1.device == result.device }) {
                    scanList.add(result)
                    Log.e(TAG, "onScanResult: " + result.scanRecord?.deviceName)
                    for (bleDevice in bleAdapter.data) {
                        if (it.isSavedDevice(bleDevice.address)) {
                            bleDevice.canScan = true
                            bleDevice.temporaryAddress = it.device.address
                            bleAdapter.notifyItemChanged(bleAdapter.getItemPosition(bleDevice))
                        }
                    }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.e(TAG, "onBatchScanResults ${results?.size}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "Scan Failed Error Code: $errorCode")
        }
    }

    override fun onStop() {
        super.onStop()
        stopServiceDiscovery()
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mScanCallback)
    }

    private fun scanBle() {
        if (!isSupportBle()) {
            showToast(getString(R.string.bluetooth_not_supported))
            return
        }

        if (bluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT)
            return
        }
        runWithPermissions(
            Permission.ACCESS_COARSE_LOCATION,
            Permission.ACCESS_FINE_LOCATION
        ) { result ->
            if (result.isAllGranted(
                    Permission.ACCESS_COARSE_LOCATION,
                    Permission.ACCESS_FINE_LOCATION
                )
            ) {
                startScan()
            }
        }
    }

    private fun isSupportBle() =
        context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            ?: false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            scanBle()
        }
    }

}