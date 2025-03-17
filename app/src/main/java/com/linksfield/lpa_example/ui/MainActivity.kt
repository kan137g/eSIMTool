package com.linksfield.lpa_example.ui

import android.content.Intent
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.linksfield.lpa_example.R
import com.linksfield.lpa_example.base.BaseActivity
import com.linksfield.lpa_example.databinding.ActivityMainBinding
import com.linksfield.lpa_example.ui.bluetooth.BleScanActivity
import com.linksfield.lpa_example.ui.wifi.WifiScanActivity
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val TAG = MainActivity::class.java.name
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun initViews() {
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_about), drawer_layout)
//                (, )R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_esim, R.id.nav_bluetooth)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.label == resources.getString(R.string.menu_home)) {
                fab.visibility = View.VISIBLE
            } else if (destination.label == resources.getString(R.string.menu_about)) {
                fab.visibility = View.GONE
            }
        }
        fab.setOnClickListener {
            when (navController.currentDestination?.label) {
                resources.getString(R.string.menu_home) -> {
                    XPopup.Builder(this)
                            .isDarkTheme(false)
                            .isDestroyOnDismiss(true)
                            //对于只使用一次的弹窗，推荐设置这个
                            .asBottomList("Please select how to add new device",
                                    arrayOf(getString(R.string.wifi), getString(R.string.bluetooth))
                            ) { _, text ->
                                when (text) {
                                    getString(R.string.wifi) -> {
                                        startActivity(Intent(this, WifiScanActivity::class.java))
                                    }
                                    getString(R.string.bluetooth) -> {
                                        startActivity(Intent(this, BleScanActivity::class.java))
                                    }
                                }
                            }
                            .show()
                }
                else -> {
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}