package com.linksfield.lpa_example.ui.home

import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseProfileActivity
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.DEVICE_NAME
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.IP_ADDRESS
import com.linksfield.lpa_example.base.BaseProfileActivity.Companion.PORT
import com.linksfield.lpa_example.data.db.WifiDevice
import com.linksfield.lpa_example.ui.wifi.WifiNSDActivity
import com.linksfield.lpa_example.ui.wifi.WifiProfileActivity

/**
 * CreateDate: 2020/8/26 18:16
 * Author: you
 * Description:
 */
class WIFIAdapter(devices: MutableList<WifiDevice>)
    : BaseQuickAdapter<WifiDevice, BaseViewHolder>(R.layout.item_bluetooth, devices) {

    override fun convert(holder: BaseViewHolder, item: WifiDevice) {
        with(item) {
            holder.setText(R.id.tv_name, name)
                    .setText(R.id.tv_address, ip)
                    .setGone(R.id.tv_connect, !canScan && connect_type == 1)
        }
        holder.getView<View>(R.id.tv_connect).setOnClickListener {
            val intent: Intent
            val ipAddress = item.ip.split(":")
            if (item.connect_type == 1) {
                intent = Intent(context, WifiNSDActivity::class.java)
                intent.putExtra(DEVICE_NAME, item.name)
                intent.putExtra(IP_ADDRESS, ipAddress[0])
                intent.putExtra(PORT, ipAddress[1])
                context.startActivity(intent)
            } else {
                intent = Intent(context, WifiProfileActivity::class.java)
                intent.putExtra(BaseProfileActivity.DATA_TYPE, item.data_type)
                intent.putExtra(DEVICE_NAME, item.name)
                intent.putExtra(IP_ADDRESS, ipAddress[0])
                intent.putExtra(PORT, ipAddress[1])
                context.startActivity(intent)
            }
        }
    }

}