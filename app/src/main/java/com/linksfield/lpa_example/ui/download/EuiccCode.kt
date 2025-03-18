package com.linksfield.lpa_example.ui.download

import android.app.PendingIntent

object EuiccCode {
    /**
     * Key for an extra set on [PendingIntent] result callbacks providing a
     * OperationCode of [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE],
     * value will be an int.
     */
    const val EXTRA_EMBEDDED_SUBSCRIPTION_OPERATION_CODE =
        "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_OPERATION_CODE"

    /**
     * Key for an extra set on [PendingIntent] result callbacks providing a
     * ErrorCode of [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE],
     * value will be an int.
     */
    const val EXTRA_EMBEDDED_SUBSCRIPTION_ERROR_CODE =
        "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_ERROR_CODE"

    /**
     * Key for an extra set on [PendingIntent] result callbacks providing a
     * SubjectCode[5.2.6.1] from GSMA (SGP.22 v2.2) decoded from
     * [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE].
     * The value of this extra will be a String.
     */
    const val EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_SUBJECT_CODE =
        "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_SMDX_SUBJECT_CODE"

    /**
     * Key for an extra set on [PendingIntent] result callbacks providing a
     * ReasonCode[5.2.6.2] from GSMA (SGP.22 v2.2) decoded from
     * [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE].
     * The value of this extra will be a String.
     */
    const val EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_REASON_CODE =
        "android.telephony.euicc.extra.EMBEDDED_SUBSCRIPTION_SMDX_REASON_CODE"
}

object OperationCode {
    /**
     * Internal system error.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_SYSTEM = 1

    /**
     * SIM slot error. Failed to switch slot, failed to access the physical slot etc.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_SIM_SLOT = 2

    /**
     * eUICC card error.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_EUICC_CARD = 3

    /**
     * Generic switching profile error
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_SWITCH = 4

    /**
     * Download profile error.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_DOWNLOAD = 5

    /**
     * Subscription's metadata error
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_METADATA = 6

    /**
     * eUICC returned an error defined in GSMA (SGP.22 v2.2) while running one of the ES10x
     * functions.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_EUICC_GSMA = 7

    /**
     * The exception of failing to execute an APDU command. It can be caused by an error
     * happening on opening the basic or logical channel, or the response of the APDU command is
     * not success (0x9000).
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_APDU = 8

    /**
     * SMDX(SMDP/SMDS) error
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_SMDX = 9

    /**
     * SubjectCode[5.2.6.1] and ReasonCode[5.2.6.2] error from GSMA (SGP.22 v2.2)
     * When [.OPERATION_SMDX_SUBJECT_REASON_CODE] is used as the
     * [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE], the remaining three bytes of the integer
     * result from [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE] will be used to stored the
     * SubjectCode and ReasonCode from the GSMA spec and NOT ErrorCode.
     *
     * The encoding will follow the format of:
     * 1. The first byte of the result will be 255(0xFF).
     * 2. Remaining three bytes(24 bits) will be split into six sections, 4 bits in each section.
     * 3. A SubjectCode/ReasonCode will take 12 bits each.
     * 4. The maximum number can be represented per section is 15, as that is the maximum number
     * allowed to be stored into 4 bits
     * 5. Maximum supported nested category from GSMA is three layers. E.g 8.11.1.2 is not
     * supported.
     *
     * E.g given SubjectCode(8.11.1) and ReasonCode(5.1)
     *
     * Base10:  0       10      8       11      1       0       5       1
     * Base2:   0000    1010    1000    1011    0001    0000    0101    0001
     * Base16:  0       A       8       B       1       0       5       1
     *
     * Thus the integer stored in [.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE] is
     * 0xA8B1051(176885841)
     *
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_SMDX_SUBJECT_REASON_CODE = 10

    /**
     * HTTP error
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val OPERATION_HTTP = 11

    const val OPERATION_NOTIFICATION = 12
}


object ErrorCode{
    /**
     * Operation such as downloading/switching to another profile failed due to device being
     * carrier locked.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_CARRIER_LOCKED = 10000

    /**
     * The activation code(SGP.22 v2.2 section[4.1]) is invalid.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_INVALID_ACTIVATION_CODE = 10001

    /**
     * The confirmation code(SGP.22 v2.2 section[4.7]) is invalid.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_INVALID_CONFIRMATION_CODE = 10002

    /**
     * The profile's carrier is incompatible with the LPA.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_INCOMPATIBLE_CARRIER = 10003

    /**
     * There is no more space available on the eUICC for new profiles.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_EUICC_INSUFFICIENT_MEMORY = 10004

    /**
     * Timed out while waiting for an operation to complete. i.e restart, disable,
     * switch reset etc.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_TIME_OUT = 10005

    /**
     * eUICC is missing or defective on the device.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_EUICC_MISSING = 10006

    /**
     * The eUICC card(hardware) version is incompatible with the software
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_UNSUPPORTED_VERSION = 10007

    /**
     * No SIM card is available in the device.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_SIM_MISSING = 10008

    /**
     * Failure to load the profile onto the eUICC card. e.g
     * 1. iccid of the profile already exists on the eUICC.
     * 2. GSMA(.22 v2.2) Profile Install Result - installFailedDueToDataMismatch
     * 3. operation was interrupted
     * 4. SIMalliance error in PEStatus(SGP.22 v2.2 section 2.5.6.1)
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_INSTALL_PROFILE = 10009

    /**
     * Failed to load profile onto eUICC due to Profile Poicly Rules.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_DISALLOWED_BY_PPR = 10010


    /**
     * Address is missing e.g SMDS/SMDP address is missing.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_ADDRESS_MISSING = 10011

    /**
     * Certificate needed for authentication is not valid or missing. E.g  SMDP/SMDS authentication
     * failed.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_CERTIFICATE_ERROR = 10012


    /**
     * No profiles available.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_NO_PROFILES_AVAILABLE = 10013

    /**
     * Failure to create a connection.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_CONNECTION_ERROR = 10014

    /**
     * Response format is invalid. e.g SMDP/SMDS response contains invalid json, header or/and ASN1.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_INVALID_RESPONSE = 10015

    /**
     * The operation is currently busy, try again later.
     * @see .EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE for details
     */
    const val ERROR_OPERATION_BUSY = 10016

    /**
     * Failure due to target port is not supported.
     * @see .switchToSubscription
     */
    const val ERROR_INVALID_PORT = 10017
}