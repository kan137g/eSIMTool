package com.linksfield.lpa_example.ui.download

import android.content.Intent
import android.telephony.euicc.EuiccManager
import android.view.View
import android_.telephony.euicc.DownloadableSubscription
import androidx.lifecycle.lifecycleScope
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.linksfield.lpa_example.App
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseActivity
import com.linksfield.lpa_example.databinding.ActivityDownloadBinding
import com.lxj.xpopup.XPopup
import com.zhouk.zxing.QRScanActivity
import kotlinx.android.synthetic.main.activity_download.activate_btn
import kotlinx.android.synthetic.main.activity_download.activate_content
import kotlinx.android.synthetic.main.activity_download.activate_done_btn
import kotlinx.android.synthetic.main.activity_download.activate_done_view
import kotlinx.android.synthetic.main.activity_download.activate_other_btn
import kotlinx.android.synthetic.main.activity_download.activate_title
import kotlinx.android.synthetic.main.activity_download.activate_view
import kotlinx.android.synthetic.main.activity_download.done_title
import kotlinx.android.synthetic.main.activity_download.error_cancel_btn
import kotlinx.android.synthetic.main.activity_download.error_content
import kotlinx.android.synthetic.main.activity_download.error_detail
import kotlinx.android.synthetic.main.activity_download.error_done_btn
import kotlinx.android.synthetic.main.activity_download.error_title
import kotlinx.android.synthetic.main.activity_download.error_view
import kotlinx.android.synthetic.main.activity_download.failed_cancel_btn
import kotlinx.android.synthetic.main.activity_download.failed_done_btn
import kotlinx.android.synthetic.main.activity_download.insert_title
import kotlinx.android.synthetic.main.activity_download.progress_view
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val RESULT_NEED_CONFIRMATION_CODE = -2

class DownloadActivity : BaseActivity<ActivityDownloadBinding>() {

    private var scanCode: String? = null
    private var confirmCode: String? = null
    private var isFromDS: Boolean = false
    private val defaultSlotId = -1

    private var mCarrierName: String? = ""
    private var isFirst = true

    override fun initViews() {
        if (isFirst) {
            scanCode = intent.getStringExtra("QR_CODE")
            confirmCode = intent.getStringExtra("CONFIRM_CODE")
            isFromDS = intent.getBooleanExtra("FROM_DS", false)
        }
        error_cancel_btn.setOnClickListener { finish() }
        error_done_btn.setOnClickListener { finish() }
        failed_cancel_btn.setOnClickListener { finish() }
        failed_done_btn.setOnClickListener { finish() }
        activate_done_btn.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        activate_other_btn.setOnClickListener {
            runWithPermissions(Permission.CAMERA) { result ->
                if (result.isAllGranted(Permission.CAMERA)) {
                    val intent = Intent(this, QRScanActivity::class.java)
                    startActivityForResult(intent, QRScanActivity.SCAN_CODE)
                }
            }
        }

        activate_btn.setOnClickListener {
            if (scanCode == null || scanCode!!.isEmpty()) {
                return@setOnClickListener
            }
            val list = scanCode!!.split('$')
            if (list.size > 4 && "1" == list[4]) {
                showConfirmationCodeDialog()
            } else {
                downloadQr(confirmCode = confirmCode)
            }
        }

        if (isFromDS) {
            downloadQr(confirmCode = confirmCode)
        } else {
            lifecycleScope.launch {
                val rsp = withContext(Dispatchers.IO) {
                    val build = DownloadableSubscription.Builder()
                        .setEncodedActivationCode(scanCode)
                        .setConfirmationCode(null)
                        .build()
                    App.INSTANCE.getLPAdClient()
                        ?.onGetDownloadableSubscriptionMetadata(defaultSlotId, build, true)
                }
                mCarrierName = rsp?.downloadableSubscription?.carrierName ?: ""
                if (rsp?.getResult() == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK) {
                    activate_title.text = getString(R.string.result_download_title, mCarrierName)
                    activate_content.text =
                        resources.getString(R.string.q_result_download_metadata_text, mCarrierName)

                    progress_view.visibility = View.GONE
                    activate_view.visibility = View.VISIBLE
                } else {
                    showErrorMessage(rsp?.getResult(), mCarrierName)
                }
            }
        }
    }

    private fun showConfirmationCodeDialog() {
        XPopup.Builder(this)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .autoOpenSoftInput(true)
            .asInputConfirm(
                getString(R.string.confirmation_code_title),
                getString(R.string.confirmation_code_text),
                "",
                "please enter the confirmation code"
            ) { code ->
                if (code.isNotEmpty()) {
                    downloadQr(confirmCode = code)
                }
            }
            .show()
    }

    private fun downloadQr(confirmCode: String? = null) {
        insert_title.text = getString(R.string.q_download_sub_title, mCarrierName)
        progress_view.visibility = View.VISIBLE
        activate_view.visibility = View.GONE

        lifecycleScope.launch {
            val resultCode = withContext(Dispatchers.IO) {
                val build = DownloadableSubscription.Builder()
                    .setEncodedActivationCode(scanCode)
                    .setConfirmationCode(confirmCode)
                    .build()
                App.INSTANCE.getLPAdClient()
                    ?.onDownloadSubscription(defaultSlotId, build, true, false)
            }

            when (resultCode) {
                EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_OK -> {
                    done_title.text = resources.getString(
                        R.string.confirm_acti_success_title,
                        mCarrierName
                    )
                    progress_view.visibility = View.GONE
                    activate_done_view.visibility = View.VISIBLE
                }

                RESULT_NEED_CONFIRMATION_CODE -> {
                    showConfirmationCodeDialog()
                }

                else -> {
                    showErrorMessage(resultCode, mCarrierName)
                }
            }
        }
    }

    private fun showErrorMessage(detailedCode: Int?, name: String?) {
        if (detailedCode == null) {
            return
        }
        val errorCodeBundle = extractErrorCode(detailedCode)
        val errorMessageUtils = DetailedErrorMessageUtils(
            this, name, null,
            errorCodeBundle.getInt(EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_ERROR_CODE),
            errorCodeBundle.getInt(EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_OPERATION_CODE),
            errorCodeBundle.getString(EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_REASON_CODE),
            errorCodeBundle.getString(EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_SUBJECT_CODE)
        )
        val errorMessages = errorMessageUtils.getErrorMessages()

        progress_view.visibility = View.GONE
        error_view.visibility = View.VISIBLE
        error_title.text = errorMessages.title
        error_content.text = errorMessages.bodyText
        val details: String? = errorMessages.errorDetails
        if (!details.isNullOrEmpty()) {
            error_detail.visibility = View.VISIBLE
            error_detail.text = details
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == QRScanActivity.SCAN_CODE) {
            progress_view.visibility = View.VISIBLE
            activate_view.visibility = View.GONE
            scanCode = data?.getStringExtra("code")
            isFirst = false
            initViews()
        }
    }
}