package com.linksfield.lpa_example.ui.wifi

import android.net.nsd.NsdServiceInfo
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.ui.wifi.ItemEntity.Companion.TYPE_NSD
import com.linksfield.lpa_example.ui.wifi.ItemEntity.Companion.TYPE_WIFI
import com.linksfield.lpad.utils.CommonUtils

/**
 * CreateDate: 2020/9/8 14:08
 * Author: you
 * Description:
 */
class WifiScanAdapter(list: MutableList<ItemEntity>) :
    BaseMultiItemQuickAdapter<ItemEntity, BaseViewHolder>(list) {

    init {
        addItemType(TYPE_WIFI, R.layout.item_wifi)
        addItemType(TYPE_NSD, R.layout.item_wifi)
        addChildClickViewIds(R.id.tv_connect)
    }

    override fun convert(holder: BaseViewHolder, item: ItemEntity) {
        when (item.itemType) {
            TYPE_WIFI -> {
                item.wifiEntity?.let {
                    holder.setText(R.id.tv_name, it.name)
                        .setText(R.id.tv_address, "${it.host}:${it.port}")
                }
            }

            TYPE_NSD -> {
                item.info?.let {
                    holder.setText(R.id.tv_name, CommonUtils.getRealName(it.serviceName))
                        .setText(R.id.tv_address, "${it.host.hostAddress}:${it.port}")
                }

            }
        }
    }

}

class ItemEntity(
    override val itemType: Int,
    val info: NsdServiceInfo? = null,
    val wifiEntity: WifiEntity? = null,
) :
    MultiItemEntity {
    companion object {
        const val TYPE_WIFI = 0
        const val TYPE_NSD = 1
    }
}

data class WifiEntity(val name: String, val host: String, val port: String)
