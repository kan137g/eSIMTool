package com.linksfield.lpa_example.ui.ds

import android_.telephony.euicc.DownloadableSubscription
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R

class DSAdapter(private val list: MutableList<DownloadableSubscription>) :
    BaseQuickAdapter<DownloadableSubscription, BaseViewHolder>(
        R.layout.item_ds, list
    ) {

    init {
        addChildClickViewIds(R.id.download_btn)
    }

    override fun convert(holder: BaseViewHolder, item: DownloadableSubscription) {
        with(item) {
            holder.setText(R.id.name_tv, carrierName)
                .setText(R.id.code_tv, getEncodedActivationCode())
        }
    }
}