package com.linksfield.lpa_example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CreateDate: 2020/8/12 18:18
 * Author: you
 * Description:
 */
@Entity(tableName = "ble_device", indices = [Index(value = ["address"], unique = true)])
data class BleDevice(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val deviceId: Long = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "address") val address: String,
) {
    @Ignore
    var canScan: Boolean = false

    @Ignore
    var temporaryAddress: String = ""
}