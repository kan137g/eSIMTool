package com.linksfield.lpa_example.ui.ds

import android.content.Intent
import android.view.View
import android_.service.euicc.GetDefaultDownloadableSubscriptionListResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.linksfield.lpa_example.App
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseActivity
import com.linksfield.lpa_example.databinding.ActivityCommonBinding
import com.linksfield.lpa_example.ui.download.DownloadActivity
import kotlinx.android.synthetic.main.activity_common.fab
import kotlinx.android.synthetic.main.app_bar_main.toolbar
import kotlinx.android.synthetic.main.recyclerview.recyclerView
import kotlinx.android.synthetic.main.recyclerview.refreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DSListActivity : BaseActivity<ActivityCommonBinding>() {

    private val defaultSlotId = -1
    private val REQUEST_CODE_DOWNLOAD = 1000

    private val dsAdapter = DSAdapter(mutableListOf())

    override fun initViews() {
        toolbar.title = "DS"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        fab.visibility = View.GONE

        recyclerView?.let {
            it.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.color_background))
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = dsAdapter
        }

        dsAdapter.setOnItemChildClickListener { _, _, position ->
            val subscription = dsAdapter.getItem(position)
            val intent = Intent(this, DownloadActivity::class.java).apply {
                putExtra("QR_CODE", subscription.getEncodedActivationCode())
                putExtra("CONFIRM_CODE", subscription.confirmationCode)
                putExtra("FROM_DS", true)
            }
            startActivityForResult(intent, REQUEST_CODE_DOWNLOAD)
        }

        refreshLayout.autoRefresh()
        refreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                val result = getDSList()
                refreshLayout.finishRefresh(result?.getResult() == 0)
                val list = result?.downloadableSubscriptions
                dsAdapter.setNewInstance(list)
                dsAdapter.setEmptyView(R.layout.view_empty)
            }
        }
    }

    private suspend fun getDSList(): GetDefaultDownloadableSubscriptionListResult? {
        return withContext(Dispatchers.IO) {
            App.INSTANCE.getLPAdClient()
                ?.onGetDefaultDownloadableSubscriptionList(defaultSlotId, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DOWNLOAD) {
            setResult(RESULT_OK)
            finish()
        }
    }
}