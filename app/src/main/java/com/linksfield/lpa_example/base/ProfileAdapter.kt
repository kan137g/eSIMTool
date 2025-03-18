package com.linksfield.lpa_example.base

import android_.service.euicc.EuiccProfileInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linksfield.lpa_example.R

/**
 * CreateDate: 2020/9/21 10:32
 * Author: you
 * Description:
 */
class ProfileAdapter (private val profileInfos: MutableList<EuiccProfileInfo>) : BaseQuickAdapter<EuiccProfileInfo,
        BaseViewHolder>(R.layout.item_profile, profileInfos) {

    init {
        addChildClickViewIds(R.id.modify_img, R.id.delete_btn, R.id.enable_btn)
    }

    override fun convert(holder: BaseViewHolder, item: EuiccProfileInfo) {
        with(item) {
            holder.setText(R.id.nickname, context.getString(R.string.nickname, nickname))
                    .setText(R.id.provider_name, context.getString(R.string.provider_name, serviceProviderName))
                    .setText(R.id.profile_name, profileName)
                    .setText(R.id.iccid, context.getString(R.string.iccid, iccid))
                    .setText(R.id.state, if (state == 1) context.getString(R.string.enabled) else context.getString(R.string.disabled))
                    .setText(R.id.enable_btn, if (state == 1) "Disable" else "Enable" )
                    .setGone(R.id.delete_btn, (state == 1))
        }
    }
}