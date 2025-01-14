package com.reactnativedailymotionsdk

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

    private var mSupportFragmentManager: FragmentManager? = null


    override fun getMainComponentName(): String = "ReactNativeDailymotionSDK"


    override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        mSupportFragmentManager = supportFragmentManager
    }

    fun getSupportFragmentManagerInstance(): FragmentManager? {
        return mSupportFragmentManager
    }
}
