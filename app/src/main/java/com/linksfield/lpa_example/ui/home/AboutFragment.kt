package com.linksfield.lpa_example.ui.home

import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseFragment
import com.linksfield.lpa_example.databinding.FragmentAboutBinding
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * CreateDate: 2020/9/22 14:17
 * Author: you
 * Description:
 */
class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    override fun initViews() {
        about_tv.text = getString(R.string.about)
    }

}