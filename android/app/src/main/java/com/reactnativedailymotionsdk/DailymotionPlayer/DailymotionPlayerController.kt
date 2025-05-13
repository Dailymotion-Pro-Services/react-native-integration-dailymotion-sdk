package com.reactnativedailymotionsdk.DailymotionPlayer

import android.view.View
import android.widget.FrameLayout
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp


class DailymotionPlayerController : SimpleViewManager<View>() {
    override fun getName(): String {
        return "DailymotionPlayerNative"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return DailymotionPlayerNativeView(reactContext)
    }

    @ReactProp(name = "playerId")
    fun setPropPlayerId(view: DailymotionPlayerNativeView, param: String?) {
        view.setPlayerId(param!!)
    }

    @ReactProp(name = "videoId")
    fun setPropVideoId(view: DailymotionPlayerNativeView, param: String?) {
        view.setVideoId(param!!)
    }

    @ReactProp(name = "playlistId")
    fun setPropPlaylistId(view: DailymotionPlayerNativeView, param: String?) {
        view.setPlaylistId(param!!)
    }

    @ReactProp(name = "playerParameters")
    fun setPropPlayerParameters(view: DailymotionPlayerNativeView, parameters: ReadableMap) {
        view.setPlayerParameters(parameters)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return MapBuilder.of<String, Any>(
            DailymotionEvent.EVENT_NAME,
            MapBuilder.of("registrationName", "onEvent")
        )
    }
}
