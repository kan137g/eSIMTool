package com.linksfield.lpa_example.ui.bluetooth

import android.bluetooth.le.ScanResult
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R

/**
 * CreateDate: 2020/8/5 17:18
 * Author: you
 * Description:
 */
class BleScanDeviceAdapter(devices: MutableList<ScanResult>) :
    BaseQuickAdapter<ScanResult, BaseViewHolder>(R.layout.item_bluetooth, devices) {

    init {
        addChildClickViewIds(R.id.tv_connect)
    }

    override fun convert(holder: BaseViewHolder, item: ScanResult) {
        holder.setText(R.id.tv_name, item.device.name)
            .setText(R.id.tv_address, item.device.address)
    }
}