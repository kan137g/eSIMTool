package com.linksfield.lpa_example.ui.download

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.linksfield.lpa_example.R

class DetailedErrorMessageUtils(
    private val context: Context,
    private val carrierName: String? = null,
    private val csTel: String? = null,
    private val errorCode: Int = 0,
    private val operationCode: Int = 0,
    private val reasonCode: String? = null,
    private val subjectCode: String? = null
) {

    fun getErrorMessages(): ErrorMessages {
        Log.d("DetailErrorMessageUtils", "operationCode = $operationCode | errorCode = $errorCode | subjectCode = $subjectCode | reasonCode = $reasonCode")
        if (operationCode == OperationCode.OPERATION_SMDX_SUBJECT_REASON_CODE) {
            return getSmdxErrorMessages()
        }
        return if (operationCode != OperationCode.OPERATION_HTTP || errorCode < 100 || errorCode > 999) {
            getGeneralErrorMessages()
        } else getNoConnectionErrorMessages()
    }

    private fun getCouldntActivateCarrierTitle() = if (TextUtils.isEmpty(this.carrierName)) {
        context.getString(R.string.title_couldnt_activate_carrier_no_name)
    } else context.getString(R.string.title_couldnt_activate_carrier, this.carrierName)

    private fun getDefaultAdditionalInfo(): String? = null

    private fun getDefaultBodyText() = context.getString(R.string.body_something_went_wrong)

    private fun getDefaultErrorDetails(): String? = null

    private fun getDefaultTitle() = context.getString(R.string.error_title)


    private fun getGeneralErrorMessages(): ErrorMessages {
        var defaultTitle: String? = getDefaultTitle()
        var defaultBodyText: String? = getDefaultBodyText()
        var defaultErrorDetails: String? = getDefaultErrorDetails()
        var defaultAdditionalInfo: String? = getDefaultAdditionalInfo()
        if ((operationCode == OperationCode.OPERATION_SWITCH || operationCode == OperationCode.OPERATION_DOWNLOAD) && this.errorCode == ErrorCode.ERROR_CARRIER_LOCKED) {
            defaultTitle = getCouldntActivateCarrierTitle()
            if (TextUtils.isEmpty(this.carrierName)) {
                defaultBodyText = context.getString(R.string.body_device_is_locked_no_name)
            } else {
                defaultBodyText =
                    context.getString(R.string.body_device_is_locked, this.carrierName)
            }
            if (TextUtils.isEmpty(this.carrierName)) {
                defaultErrorDetails =
                    context.getString(R.string.detail_contact_carrier_to_unlock_no_name)
            } else if (TextUtils.isEmpty(this.csTel)) {
                defaultErrorDetails = context.getString(
                    R.string.detail_contact_carrier_to_unlock_no_tel, this.carrierName
                )
            } else {
                defaultErrorDetails = context.getString(
                    R.string.detail_contact_carrier_to_unlock, this.carrierName, this.csTel
                )
            }
        } else if (operationCode == OperationCode.OPERATION_EUICC_GSMA && this.errorCode == ErrorCode.ERROR_EUICC_INSUFFICIENT_MEMORY) {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = context.getString(R.string.body_not_enough_space_to_download)
            defaultErrorDetails =
                context.getString(R.string.detail_delete_a_profile_before_retrying)
            defaultAdditionalInfo = null
            return ErrorMessages(
                defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
            )
        } else if (operationCode == OperationCode.OPERATION_EUICC_GSMA && this.errorCode == ErrorCode.ERROR_INSTALL_PROFILE) {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails =
                getProvideInfoToCarrierErrorDetails(this.operationCode, this.errorCode)
            return ErrorMessages(
                defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
            )
        } else if (operationCode == OperationCode.OPERATION_EUICC_GSMA && !(10000..10017).contains(
                errorCode
            )
        ) {
            val operateCode = (errorCode and 0xFF0000) ushr 16
            val euiccResult = errorCode and 0xFFFF
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails = context.getString(
                R.string.detail_provide_info_to_carrier,
                "operateCode: $operateCode",
                "euiccResult: $euiccResult"
            )
            return ErrorMessages(
                defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
            )
        } else if (operationCode == OperationCode.OPERATION_NOTIFICATION) {
            defaultTitle = getDefaultTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails = null
            return ErrorMessages(
                defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
            )
        } else {
            if (operationCode == OperationCode.OPERATION_SMDX) {
                if (arrayOf(
                        ErrorCode.ERROR_NO_PROFILES_AVAILABLE,
                        ErrorCode.ERROR_CONNECTION_ERROR,
                        ErrorCode.ERROR_CERTIFICATE_ERROR,
                        ErrorCode.ERROR_INVALID_RESPONSE,
                        ErrorCode.ERROR_INVALID_CONFIRMATION_CODE
                    ).contains(Integer.valueOf(this.errorCode))
                ) {
                    defaultTitle = getCouldntActivateCarrierTitle()
                    defaultBodyText = getIssueWithSimProfileBodyText()
                    defaultErrorDetails =
                        getProvideInfoToCarrierErrorDetails(this.operationCode, this.errorCode)
                    return ErrorMessages(
                        defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
                    )
                }
            }
            if (operationCode == OperationCode.OPERATION_METADATA && this.errorCode == ErrorCode.ERROR_INVALID_ACTIVATION_CODE) {
                defaultTitle = context.getString(R.string.title_incorrect_qr_code)
                defaultBodyText = context.getString(R.string.body_check_qr_code_or_go_back)
                defaultErrorDetails =
                    context.getString(R.string.detail_contact_carrier_for_more_help)
                defaultAdditionalInfo = null
                return ErrorMessages(
                    defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
                )
            }
            if (operationCode == OperationCode.OPERATION_METADATA && this.errorCode == ErrorCode.ERROR_INVALID_PORT) {
                defaultTitle = context.getString(R.string.title_connect_to_wifi)
                defaultBodyText = context.getString(R.string.body_need_wifi_connection)
                defaultErrorDetails = context.getString(R.string.detail_try_adding_again)
            }
            return ErrorMessages(
                defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
            )
        }
        defaultAdditionalInfo = null
        return ErrorMessages(
            defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
        )
    }

    private fun getSmdxErrorMessages(): ErrorMessages {
        var defaultTitle: String? = getDefaultTitle()
        var defaultBodyText: String? = getDefaultBodyText()
        var defaultErrorDetails: String? = getDefaultErrorDetails()
        var defaultAdditionalInfo: String? = getDefaultAdditionalInfo()
        if (this.subjectCode == "8.1" && this.reasonCode == "4.8") {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = context.getString(R.string.body_not_enough_space_to_download)
            defaultErrorDetails =
                context.getString(R.string.detail_delete_a_profile_before_retrying)
            defaultAdditionalInfo = null
        } else if (this.subjectCode == "8.1.1" && this.reasonCode == "3.8") {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails =
                getProvideInfoToCarrierErrorDetails(this.subjectCode, this.reasonCode)
        } else if (this.subjectCode == "8.2.6" && this.reasonCode == "3.8") {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails =
                getProvideInfoToCarrierErrorDetails(this.subjectCode, this.reasonCode)
        } else if (this.subjectCode == "8.2.7" && this.reasonCode == "6.4") {
            defaultTitle = getCouldntActivateCarrierTitle()
            defaultBodyText = getIssueWithSimProfileBodyText()
            defaultErrorDetails =
                getProvideInfoToCarrierErrorDetails(this.subjectCode, this.reasonCode)
        } else if (this.subjectCode == "8.2" && this.reasonCode == "1.2") {
            defaultTitle = context.getString(R.string.title_profile_already_downloaded)
            if (TextUtils.isEmpty(this.carrierName)) {
                defaultBodyText =
                    context.getString(R.string.body_make_sure_not_already_downloaded_no_name)
            } else if (TextUtils.isEmpty(this.csTel)) {
                defaultBodyText = context.getString(
                    R.string.body_make_sure_not_already_downloaded_no_tel, this.carrierName
                )
            } else {
                defaultBodyText = context.getString(
                    R.string.body_make_sure_not_already_downloaded, this.carrierName, this.csTel
                )
            }
            defaultErrorDetails =
                getProvideInfoToCarrierErrorDetails(this.subjectCode, this.reasonCode)
        }
        return ErrorMessages(
            defaultTitle, defaultBodyText, defaultErrorDetails, defaultAdditionalInfo
        )
    }

    private fun getIssueWithSimProfileBodyText(): String {
        if (TextUtils.isEmpty(carrierName)) {
            return context.getString(R.string.body_issue_with_sim_profile_no_name)
        }
        return if (TextUtils.isEmpty(csTel)) {
            context.getString(R.string.body_issue_with_sim_profile_no_tel, carrierName)
        } else context.getString(R.string.body_issue_with_sim_profile, carrierName, csTel)
    }

    private fun getProvideInfoToCarrierErrorDetails(operationCode: Int, errorCode: Int): String {
        return context.getString(
            R.string.detail_provide_info_to_carrier,
            "${context.getString(R.string.operation_code_text)}: $operationCode",
            "${context.getString(R.string.error_code_text)}: $errorCode"
        )
    }

    private fun getProvideInfoToCarrierErrorDetails(subjectCode: String, reasonCode: String): String {
        return context.getString(
            R.string.detail_provide_info_to_carrier,
            "${context.getString(R.string.subject_code_text)}: $subjectCode",
            "${context.getString(R.string.reason_code_text)}: $reasonCode"
        )
    }

    private fun getNoConnectionErrorMessages(): ErrorMessages {
        return ErrorMessages(
            context.getString(R.string.title_no_connection),
            context.getString(R.string.body_need_wifi_or_mobile_connection),
            context.getString(R.string.detail_try_adding_again),
            null
        )
    }
}


