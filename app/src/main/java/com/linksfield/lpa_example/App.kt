package com.linksfield.lpa_example

import android.app.Application
import android.os.Process
import android.text.TextUtils
import com.linksfield.lpad.grpc.LPAdClient
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


/**
 * CreateDate: 2020/8/5 16:45
 * Author: you
 * Description:
 */
class App : Application() {

    private var lpadClient: LPAdClient? = null

    fun getLPAdClient(): LPAdClient? = lpadClient

    fun setLPAdClient(client: LPAdClient?) {
        this.lpadClient = client
    }

    companion object {
        lateinit var INSTANCE: App

        init {
            //设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white)//全局设置主题颜色
                MaterialHeader(context)//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        initBugly()
    }

    private fun initBugly() {
        // 获取当前包名
        val packageName = applicationContext.packageName
        // 获取当前进程名
        val processName: String? = getProcessName(Process.myPid())
        // 设置是否为上报进程
        val strategy = UserStrategy(applicationContext)
        strategy.isUploadProcess = false || processName == packageName
        CrashReport.initCrashReport(applicationContext, "97cc2dfd54", false, strategy)
    }

    fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }

}