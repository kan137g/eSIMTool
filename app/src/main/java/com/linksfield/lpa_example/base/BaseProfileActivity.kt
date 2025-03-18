package com.linksfield.lpa_example.base

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.linksfield.lpa_example.App
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.data.db.BleDevice
import com.linksfield.lpa_example.data.db.WifiDevice
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpa_example.ui.download.DownloadActivity
import com.linksfield.lpa_example.ui.ds.DSListActivity
import com.linksfield.lpa_example.ui.home.HomeViewModel
import com.linksfield.lpad.device.AbstractPbDevice
import com.linksfield.lpad.device.EuiccBleDevice
import com.linksfield.lpad.device.EuiccWifiDevice
import com.linksfield.lpad.device.network.NetworkOption
import com.linksfield.lpad.utils.ConnectionChangedListener
import com.linksfield.lpad.utils.State
import com.lxj.xpopup.XPopup
import com.zhouk.zxing.QRScanActivity
import kotlinx.android.synthetic.main.app_bar_main.fab
import kotlinx.android.synthetic.main.app_bar_main.toolbar
import kotlinx.android.synthetic.main.recyclerview.recyclerView
import kotlinx.android.synthetic.main.recyclerview.refreshLayout
import kotlinx.android.synthetic.main.toolbar.patch_btn
import kotlinx.android.synthetic.main.toolbar.status_tv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * CreateDate: 2020/9/18 15:41
 * Author: you
 * Description:
 */
abstract class BaseProfileActivity<VB : ViewBinding> : BaseActivity<ActivityCommonBinding>(),
    ConnectionChangedListener {

    //wifi使用
    companion object {
        val TAG = BaseProfileActivity::class.java.name
        const val DEVICE_NAME = "DEVICE_NAME"
        const val IP_ADDRESS = "IP_ADDRESS"
        const val PORT = "PORT"

        //1代表protobuf 2代表json
        const val DATA_TYPE = "DATA_TYPE"
        const val REQUEST_CODE_DOWNLOAD = 1001
        const val REQUEST_CODE_DOWNLOAD_DS = 1002

        const val CHECK_PATCH =
            "00A4000C02ABCESW9000\\r\\n00A4000C02ABCDSW9000\\r\\n00A4000C02ABCESW9000\\r\\n00B000001DSWFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000\\r\\n"
        const val PATCH =
            "00A4000C02ABCESW9000\\r\\n00D600001DCC45437531303133652024070858DD77C105282D279F0EB4C3F1D386E7SW9000\\r\\n00A4000C02ABCDSW9000\\r\\n00D60000486F062152C69B5D90454375313031336520240708001C000E3186942AB3F4DA8045C63BE2852DA9AC4BCF110AF43C7ECB22F89CCE1E7FDD1BB84D8FBF1C385B5406238CBA5D3467C8SW9000\\r\\n00A4000C02ABCESW9000\\r\\n00D600001D01454375313031336520240708A2A65F8E9D24A1418B79567D1F36D2CBSW9000\\r\\n00B000001DSW02454375313031336520240708000000000000000000000000000000009000\\r\\n"

    }

    private lateinit var eidTv: TextView
    private val homeViewModel: HomeViewModel = HomeViewModel.getInstance()
    private val defaultSlotId = -1

    var device: AbstractPbDevice? = null

    private val mAdapter = ProfileAdapter(mutableListOf())

    //默认蓝牙连接
    var type = ConnectType.BLE

    //蓝牙连接使用
    var bleDevice: BleDevice? = null

    //wifi连接使用
    var ipAddress = ""

    private var deviceName: String? = ""
    private var needResetDevice: Boolean = false
    private var isDownloading: Boolean = false

    abstract fun setConnectType()
    abstract fun checkConnectStateAndUpdateProfileView()

    val option = NetworkOption()

    override fun initViews() {
        intent.getStringExtra(IP_ADDRESS)?.let {
            option.ip = it
        }
        intent.getStringExtra(PORT)?.let {
            option.port = it.toInt()
        }
        ipAddress = "${option.ip}:${option.port}"

        setConnectType()
        deviceName = intent.getStringExtra(DEVICE_NAME)
        toolbar.title = if (type == ConnectType.BLE) "Bluetooth" else deviceName ?: "WIFI"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        patch_btn.setOnClickListener {
            lifecycleScope.launch {
                val checkResult = checkPatchSuccess()
                when (checkResult) {
                    CheckPatchResult.AlreadyApplied -> showToast("Patch is already applied!")
                    CheckPatchResult.NoPatch -> {
                        showLoading("Patching!")
                        val patchResult = patch()
                        dismissLoading()
                        when (patchResult) {
                            PatchResult.Succeed -> showToast("Success!")
                            PatchResult.AlreadyApplied -> showToast("Patch is already applied!")
                            PatchResult.Failed -> showToast("Patch Failed!")
                        }
                    }

                    CheckPatchResult.Failed -> showToast("Check Patch Failed!")
                }
            }
        }

        recyclerView?.let {
//            it.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.color_background))
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = mAdapter
        }

        refreshLayout.autoRefresh()
        refreshLayout.setOnRefreshListener {
            checkConnectStateAndUpdateProfileView()
        }
        eidTv = layoutInflater.inflate(R.layout.view_eid, null, false) as TextView
        mAdapter.addHeaderView(eidTv)
        mAdapter.headerWithEmptyEnable = true
        mAdapter.setOnItemChildClickListener { _, view, position ->
            val iccid = mAdapter.getItem(position).iccid
            when (view.id) {
                R.id.modify_img -> {
                    XPopup.Builder(this).isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                        .autoOpenSoftInput(true).asInputConfirm(
                            "Rename", "", "", "please input SIM card name"
                        ) { text ->
                            lifecycleScope.launch {
                                refreshLayout.autoRefreshAnimationOnly()
                                val result = withContext(Dispatchers.IO) {
                                    App.INSTANCE.getLPAdClient()
                                        ?.onUpdateSubscriptionNickname(defaultSlotId, iccid, text)
                                }
                                setOperationResult(result)
                            }
                        }.show()
                }

                R.id.delete_btn -> {
                    val popupView = XPopup.Builder(this) //
                        .asConfirm(
                            "Delete", "Confirm to delete?", "Cancel", "Delete", {
                                lifecycleScope.launch {
                                    refreshLayout.autoRefreshAnimationOnly()
                                    val result = withContext(Dispatchers.IO) {
                                        App.INSTANCE.getLPAdClient()
                                            ?.onDeleteSubscription(defaultSlotId, iccid)
                                    }
                                    setOperationResult(result)
                                }
                            }, null, false
                        )
                    popupView.confirmTextView.setTextColor(
                        ContextCompat.getColor(
                            this, R.color.status_red
                        )
                    )
                    popupView.show()
                }

                R.id.enable_btn -> {
                    val item = mAdapter.getItem(position)
                    refreshLayout.autoRefreshAnimationOnly()
                    needResetDevice = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        val lpAdClient = App.INSTANCE.getLPAdClient()
                        val result = if (item.state == 1) {
                            lpAdClient?.onDisableSubscription(defaultSlotId, iccid)
                        } else {
                            lpAdClient?.onSwitchToSubscription(defaultSlotId, iccid, false)
                        }
                        setOperationResult(result)
                    }
                }
            }
        }

        fab.setOnClickListener {
            val items = arrayOf("Scan", "DS")
            val builder = AlertDialog.Builder(this)
            builder.setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        runWithPermissions(Permission.CAMERA) { result ->
                            if (result.isAllGranted(Permission.CAMERA)) {
                                isDownloading = true
                                val intent = Intent(this, QRScanActivity::class.java)
                                startActivityForResult(intent, QRScanActivity.SCAN_CODE)
                            }
                        }
                    }

                    1 -> {
                        isDownloading = true
                        val intent = Intent(this, DSListActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_DS)
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private suspend fun setOperationResult(result: Int?) {
        if (result == 0) {
            if (needResetDevice) {
                withContext(Dispatchers.Main) {
                    refreshLayout.autoRefreshAnimationOnly()
                }
                withContext(Dispatchers.IO) {
                    device?.resetDevice()
                    needResetDevice = false
                }
            }
            updateProfileView()
        } else {
            withContext(Dispatchers.Main) {
                refreshLayout.finishRefresh(false)
                showToast("operation failed!")
            }
            Log.e(TAG, "error: resultCode = $result")
        }
    }

    fun listenConnectState() {
        if (device is EuiccWifiDevice) {
            (device as EuiccWifiDevice).setOnConnectionStatusListener(this)
        } else if (device is EuiccBleDevice) {
            (device as EuiccBleDevice).setOnConnectionStatusListener(this)
        }
    }

    suspend fun updateProfileView() {
        val eid = withContext(Dispatchers.IO) {
            App.INSTANCE.getLPAdClient()?.onGetEid(defaultSlotId)
        }
        withContext(Dispatchers.Main) {
            eidTv.text = getString(R.string.eid, eid)
        }

        val profiles = withContext(Dispatchers.IO) {
            App.INSTANCE.getLPAdClient()?.onGetEuiccProfileInfoList(defaultSlotId)
        }
        withContext(Dispatchers.Main) {
            if (profiles?.getResult() == 0) {
                val dataType = intent.getIntExtra(DATA_TYPE, 0)
                when (type) {
                    ConnectType.BLE -> bleDevice?.let { homeViewModel.addBleDevice(it) }
                    ConnectType.NSD -> homeViewModel.addWifiDevice(
                        WifiDevice(
                            name = deviceName,
                            ip = ipAddress,
                            connect_type = 1,
                            data_type = dataType
                        )
                    )

                    ConnectType.WIFI -> homeViewModel.addWifiDevice(
                        WifiDevice(
                            name = deviceName,
                            ip = ipAddress,
                            connect_type = 2,
                            data_type = dataType
                        )
                    )
                }
                status_tv.text = getString(R.string.state_connected)
                profiles.profiles?.toList()?.let {
                    mAdapter.setNewInstance(profiles.profiles?.toMutableList())
                    mAdapter.setEmptyView(R.layout.view_empty)
                    refreshLayout.finishRefresh()
                }

            } else {
                refreshLayout.finishRefresh(false)
                showToast("operation failed!")
                Log.e(TAG, "error: resultCode = ${profiles?.getResult()}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.SCAN_CODE) {
                val intent = Intent(this, DownloadActivity::class.java).apply {
                    putExtra("QR_CODE", data?.getStringExtra("code"))
                }
                startActivityForResult(intent, REQUEST_CODE_DOWNLOAD)
            } else if (requestCode == REQUEST_CODE_DOWNLOAD || requestCode == REQUEST_CODE_DOWNLOAD_DS) {
                needResetDevice = true
                lifecycleScope.launch {
                    setOperationResult(0)
                    isDownloading = false
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isDownloading) {
            lifecycleScope.launch(Dispatchers.IO) {
                delay(2 * 60 * 1000)
                device?.close()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        device?.close()
    }

    enum class ConnectType {
        BLE, NSD, //nsd搜索后连接
        WIFI //输入ip 端口连接
    }

    override fun onConnectionChanged(status: Int) {
        runOnUiThread {
            status_tv.visibility = View.VISIBLE
            when (status) {
                State.STATE_EXCEPTION -> {
                    status_tv.text = getString(R.string.state_error)
                    mAdapter.setNewInstance(null)
                    refreshLayout.finishRefresh(false)
                    device?.close()
                }

                State.STATE_TIME_OUT -> {
                    status_tv.text = getString(R.string.state_time_out)
                    mAdapter.setNewInstance(null)
                    refreshLayout.finishRefresh(false)
                }

                State.STATE_DISCONNECTED -> status_tv.text = getString(R.string.state_disconnected)
                State.STATE_CONNECTING -> status_tv.text = getString(R.string.state_connecting)
                State.STATE_CONNECTED -> {
                    status_tv.text = getString(R.string.state_connected)
                    patch_btn.visibility = View.VISIBLE
                }

                State.STATE_DISCONNECTING -> status_tv.text =
                    getString(R.string.state_disconnecting)
            }
        }
    }

    private suspend fun checkPatchSuccess(): CheckPatchResult = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            device?.iccTransmitApduCommands(
                CHECK_PATCH
            ) { result, response ->
                Log.d(
                    TAG,
                    "checkPatchSuccess result = $result, response = ${response.contentToString()}"
                )
                continuation.resume(
                    when {
                        result == -12 -> CheckPatchResult.AlreadyApplied
                        result >= 0 -> CheckPatchResult.NoPatch
                        else -> CheckPatchResult.Failed
                    }
                )
            }
        }
    }

    private suspend fun patch(): PatchResult = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            device?.iccTransmitApduCommands(
                PATCH
            ) { result, response ->
                Log.d(TAG, "patch result = $result, response = ${response.contentToString()}")
                continuation.resume(
                    when {
                        result >= 0 -> PatchResult.Succeed
                        result == -12 -> PatchResult.AlreadyApplied
                        else -> PatchResult.Failed
                    }
                )
            }
        }
    }

}

enum class CheckPatchResult {
    AlreadyApplied, NoPatch, Failed,
}

enum class PatchResult {
    Succeed, AlreadyApplied, Failed,
}