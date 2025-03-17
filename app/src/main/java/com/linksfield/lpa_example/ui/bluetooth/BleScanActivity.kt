package com.linksfield.lpa_example.ui.bluetooth

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityOptions
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseActivity
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpad.utils.BLE_SCAN_PERIOD
import com.linksfield.lpad.utils.filterUuid
import com.zhouk.zxing.QRScanActivity
import kotlinx.android.synthetic.main.activity_common.fab
import kotlinx.android.synthetic.main.app_bar_main.toolbar
import kotlinx.android.synthetic.main.recyclerview.recyclerView

/**
 * CreateDate: 2020/8/4 14:17
 * Author: you
 * Description:
 */
class BleScanActivity : BaseActivity<ActivityCommonBinding>() {

    private val TAG = BleScanActivity::class.java.name
    lateinit var mAdapter: BleScanDeviceAdapter
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var animator: ObjectAnimator? = null
    private var isScanning = false

    companion object {
        const val REQUEST_CODE_ENABLE_BT = 100
        const val REQUEST_CODE_SCAN_BT = 101
        const val REQUEST_CODE_SCAN = 102
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_scan ->
                runWithPermissions(Permission.CAMERA) { result ->
                    if (result.isAllGranted(Permission.CAMERA)) {
                        val intent = Intent(this, QRScanActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_SCAN)
                    }
                }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initViews() {
        mAdapter = BleScanDeviceAdapter(mutableListOf())
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        fab.setImageResource(R.drawable.ic_bluetooth)
        fab.setOnClickListener {
            scanBleDevices()
        }

        toolbar.title = "Bluetooth"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        scanBleDevices()

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        mAdapter.setOnItemChildClickListener { _, view, position ->
            val result = mAdapter.getItem(position)
            connectDevice(result, view = view)
        }
    }

    private fun connectDevice(result: ScanResult, view: View? = null) {
        val intent = Intent(this, BleProfileActivity::class.java)
        intent.putExtra(BleProfileActivity.EXTRAS_DEVICE_NAME, result.device.name)
        intent.putExtra(BleProfileActivity.EXTRAS_DEVICE_ADDRESS, result.device.address)
        if (isScanning) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(mScanCallback)
            isScanning = false
            stopLoadingAnim()
        }
        val options = view?.let {
            ActivityOptions.makeSceneTransitionAnimation(
                this, it.parent as View, getString(R.string.share_view)
            )
        }
        startActivity(intent, options?.toBundle())
    }

    private fun startScan() {
        if (isScanning) {
            return
        }

        //SCAN_MODE_LOW_POWER--------耗电最少，扫描时间间隔最短
        //SCAN_MODE_BALANCED---------平衡模式，耗电适中，扫描时间间隔一般，我使用这种模式来更新设备状态
        //SCAN_MODE_LOW_LATENCY---------最耗电，扫描延迟时间短，打开扫描需要立马返回结果可以使用
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //前台扫描
                .build()
        val scanFilter = ScanFilter.Builder()
//            .setServiceUuid(ParcelUuid.fromString(EuiccGattAttributes.EUICC_SERVICE))
            .build()

        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothAdapter.bluetoothLeScanner.stopScan(mScanCallback)
            isScanning = false
            stopLoadingAnim()
        }, BLE_SCAN_PERIOD)

        startLoadingAnim()
        isScanning = true
        mAdapter.setEmptyView(R.layout.view_empty)
        mAdapter.setNewInstance(mutableListOf())
        bluetoothAdapter.bluetoothLeScanner.startScan(
            mutableListOf(scanFilter), scanSettings, mScanCallback
        )
    }

    private val mScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let {
                if (result.filterUuid() && !mAdapter.data.any { it2 -> it2.device.address == it.address }) {
                    mAdapter.addData(result)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.e(TAG, "onBatchScanResults ${results?.size}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            stopLoadingAnim()
            Log.e(TAG, "Scan Failed Error Code: $errorCode")
        }
    }

    private fun scanBleDevices() {
        if (packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) != true) {
            showToast(getString(R.string.bluetooth_not_supported))
            return
        }

        runWithPermissions(
            Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION
        ) { result ->
            if (result.isAllGranted(
                    Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION
                )
            ) {
                if (isAndroid12()) {
                    if (hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        openBle()
                    } else {
                        ActivityCompat.requestPermissions(
                            this, arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ), REQUEST_CODE_SCAN_BT
                        )
                    }
                } else {
                    openBle()
                }
            }
        }
    }

    private fun openBle() {
        if (bluetoothAdapter.isEnabled) {
            startScan()
            return
        }
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ENABLE_BT -> {
                    showToast(if (bluetoothAdapter.isEnabled) "蓝牙已打开" else "蓝牙未打开")
                    if (bluetoothAdapter.isEnabled) {
                        startScan()
                    }
                }

                REQUEST_CODE_SCAN -> {
                    data?.getStringExtra("code")?.let { code ->
                        mAdapter.data.find { result ->
                            result.scanRecord?.serviceUuids?.any {
                                it.uuid.toString()
                                    .contentEquals(code, ignoreCase = true)
                            } == true
                        }?.let(::connectDevice)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_SCAN_BT -> {
                if (grantResults.isNotEmpty() && grantResults.all {
                        it == PackageManager.PERMISSION_GRANTED
                    }) openBle()
            }
        }
    }

    private fun hasPermission(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun isAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    private fun startLoadingAnim() {
        fab.setImageResource(R.drawable.ic_loading)
        animator = ObjectAnimator.ofFloat(fab, "rotation", 0f, 360f)
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.duration = 800
        animator?.interpolator = LinearInterpolator()
        animator?.start()
    }

    private fun stopLoadingAnim() {
        fab.setImageResource(R.drawable.ic_bluetooth)
        animator?.cancel()
        fab.rotation = 0f
    }
}