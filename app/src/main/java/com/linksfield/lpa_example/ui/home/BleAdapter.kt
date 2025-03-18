package com.linksfield.lpa_example.ui.home

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.data.db.BleDevice

/**
 * CreateDate: 2020/8/13 10:16
 * Author: you
 * Description:
 */
class BleAdapter(devices: MutableList<BleDevice>)
    : BaseQuickAdapter<BleDevice, BaseViewHolder>(R.layout.item_bluetooth, devices) {

    init {
        addChildClickViewIds(R.id.tv_connect)
    }
    override fun convert(holder: BaseViewHolder, item: BleDevice) {
        with(item) {
            holder.setText(R.id.tv_name, name)
                    .setText(R.id.tv_address, address)
                    .setGone(R.id.tv_connect, !canScan)
        }
    }
}