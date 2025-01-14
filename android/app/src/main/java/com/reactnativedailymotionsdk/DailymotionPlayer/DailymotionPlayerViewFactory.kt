package com.reactnativedailymotionsdk.DailymotionPlayer

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext


class DailymotionPlayerViewFactory: ReactPackage {
    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<DailymotionPlayerController> {
        return mutableListOf(
            DailymotionPlayerController()
        )
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
        listOf(DailymotionPlayerNativeModule(reactContext)).toMutableList()
}