package com.linksfield.lpa_example

import java.util.Stack

fun main() {
    extractErrorCode(176336947)
}

fun extractErrorCode(detailedCode: Int) {
    val operationCode = detailedCode ushr 24
    if (operationCode == 10) {
        val codeList: List<String> =
            decodeSmdxSubjectAndReasonCode(detailedCode)
        val subjectCode = codeList[0]
        val reasonCode = codeList[1]
        print("operationCode = $operationCode | subjectCode = $subjectCode | reasonCode = $reasonCode")
    } else {
        val errorCode = detailedCode and 0xFFFFFF
        print("operationCode = $operationCode | errorCode = $errorCode")
    }
}

fun decodeSmdxSubjectAndReasonCode(resultCode: Int): List<String> {
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
    return listOf(subjectCode, reasonCode)
}
