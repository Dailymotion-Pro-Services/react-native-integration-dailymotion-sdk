package com.newreactnativedailymotionsdk.DailymotionPlayer

import android.view.View
import android.widget.FrameLayout
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class DailymotionPlayerController : SimpleViewManager<View>() {

    override fun getName(): String = "DailymotionPlayerNative"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return DailymotionPlayerNativeView(reactContext)
    }

    @ReactProp(name = "playerId")
    fun setPropPlayerId(view: DailymotionPlayerNativeView, param: String?) {
        view.setPlayerId(param ?: return)
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

    override fun onAfterUpdateTransaction(view: View) {
        super.onAfterUpdateTransaction(view)
        (view as? DailymotionPlayerNativeView)?.loadPlayerIfReady()
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return MapBuilder.of(
            DailymotionEvent.EVENT_NAME,
            MapBuilder.of("registrationName", "onEvent")
        )
    }
}
