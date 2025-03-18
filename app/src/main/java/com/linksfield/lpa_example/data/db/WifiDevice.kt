package com.linksfield.lpa_example.data.db

import androidx.room.*

/**
 * CreateDate: 2020/8/26 17:13
 * Author: you
 * Description:
 */
@Entity(tableName = "wifi_device", indices = [Index(value = ["ip"], unique = true)])
data class WifiDevice(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val deviceId: Long = 0,
        @ColumnInfo(name = "name") val name: String? = "",
        @ColumnInfo(name = "ip") val ip: String,
        //首页跳转，nsd还是手动输入标志 1 nsd 2 手动输入
        @ColumnInfo(name = "connect_type") val connect_type: Int = 1,
        //1代表protobuf 2代表json
        @ColumnInfo(name = "data_type") val data_type: Int = 1
) {
    @Ignore
    var canScan: Boolean = false
}