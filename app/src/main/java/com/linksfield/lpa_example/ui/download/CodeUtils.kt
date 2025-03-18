package com.linksfield.lpa_example.ui.download

import android.os.Bundle
import android.util.Pair
import java.util.*

fun extractErrorCode(detailedCode: Int): Bundle {
    val bundle = Bundle()
    val operationCode = detailedCode ushr 24
    bundle.putInt(EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_OPERATION_CODE, operationCode)
    if (operationCode == OperationCode.OPERATION_SMDX_SUBJECT_REASON_CODE) {
        val decodeSmdxSubjectAndReasonCode: Pair<String, String> =
            decodeSmdxSubjectAndReasonCode(detailedCode)
        bundle.putString(
            EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_SUBJECT_CODE,
            decodeSmdxSubjectAndReasonCode.first
        )
        bundle.putString(
            EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_SMDX_REASON_CODE,
            decodeSmdxSubjectAndReasonCode.second
        )
    } else {
        bundle.putInt(
            EuiccCode.EXTRA_EMBEDDED_SUBSCRIPTION_ERROR_CODE,
            detailedCode and 0xFFFFFF
        )
    }
    return bundle
}

/**
 * Given encoded error code described in
 * [android.telephony.euicc.EuiccManager.OPERATION_SMDX_SUBJECT_REASON_CODE] decode it
 * into SubjectCode[5.2.6.1] and ReasonCode[5.2.6.2] from GSMA (SGP.22 v2.2)
 *
 * @param resultCode from
 * [android.telephony.euicc.EuiccManager.OPERATION_SMDX_SUBJECT_REASON_CODE]
 * @return a pair containing SubjectCode[5.2.6.1] and ReasonCode[5.2.6.2] from GSMA (SGP.22
 * v2.2)
 */
fun decodeSmdxSubjectAndReasonCode(resultCode: Int): Pair<String, String> {
    var code = resultCode
    val numOfSections = 6
    val bitsPerSection = 4
    val sectionMask = 0xF
    val sections: Stack<Int> = Stack()

    // Extracting each section of digits backwards.
    for (i in 0 until numOfSections) {
        val sectionDigit = code and sectionMask
        sections.push(sectionDigit)
        code = code ushr bitsPerSection
    }
    var subjectCode: String =
        (sections.pop().toString() + "." + sections.pop()) + "." + sections.pop()
    var reasonCode: String =
        (sections.pop().toString() + "." + sections.pop()) + "." + sections.pop()

    // drop the leading zeros, e.g 0.1 -> 1, 0.0.3 -> 3, 0.5.1 -> 5.1
    subjectCode = subjectCode.replace("^(0\\.)*".toRegex(), "")
    reasonCode = reasonCode.replace("^(0\\.)*".toRegex(), "")
    return Pair.create(subjectCode, reasonCode)
}

