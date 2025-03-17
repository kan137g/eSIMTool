package com.linksfield.lpa_example.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import java.lang.reflect.ParameterizedType

/**
 * CreateDate: 2020/8/28 11:18
 * Author: you
 * Description:
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private val TAG = BaseActivity::class.java.name

    private lateinit var binding: VB
    private var loadingPopup: LoadingPopupView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val clazz = type.actualTypeArguments[0] as Class<VB>
                val method = clazz.getMethod("inflate", LayoutInflater::class.java)
                binding = method.invoke(null, layoutInflater) as VB
                setContentView(binding.root)
                initViews()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    abstract fun initViews()

    fun showLoading(tips: String) {
        if (loadingPopup == null) {
            loadingPopup = XPopup.Builder(this).asLoading(tips)
        }
        loadingPopup?.show()
    }

    fun dismissLoading() {
        if (loadingPopup?.isShow == true) {
            loadingPopup?.dismiss()
        }
    }

    fun showToast(content: String) {
        runOnUiThread {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }
}