package com.newreactnativedailymotionsdk.DailymotionPlayer

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil

class DailymotionPlayerNativeModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "DailymotionPlayerNative"

    companion object {
        private const val TAG = "--DailymotionModule--"
    }

    private fun withPlayerView(reactTag: Int, action: (DailymotionPlayerNativeView) -> Unit) {
        val view = DailymotionPlayerNativeView.viewRegistry[reactTag]
        if (view == null) {
            Log.w(TAG, "No view found for reactTag=$reactTag registry=${DailymotionPlayerNativeView.viewRegistry.keys}")
            return
        }
        UiThreadUtil.runOnUiThread { action(view) }
    }

    @ReactMethod
    fun loadContent(reactTag: Int, videoId: String, playlistId: String?, startTime: Double) {
        withPlayerView(reactTag) { it.loadContent(videoId, playlistId, startTime) }
    }

    @ReactMethod
    fun play(reactTag: Int) {
        withPlayerView(reactTag) { it.play() }
    }

    @ReactMethod
    fun pause(reactTag: Int) {
        withPlayerView(reactTag) { it.pause() }
    }

    @ReactMethod
    fun setFullscreen(reactTag: Int, fullscreen: Boolean, orientation: String) {
        withPlayerView(reactTag) { it.setFullscreen(fullscreen, orientation) }
    }

    @ReactMethod
    fun setSubtitles(reactTag: Int, wantedSubtitle: String) {
        withPlayerView(reactTag) { it.setSubtitles(wantedSubtitle) }
    }

    @ReactMethod
    fun setQuality(reactTag: Int, wantedQuality: String) {
        withPlayerView(reactTag) { it.setQuality(wantedQuality) }
    }

    @ReactMethod
    fun seekTo(reactTag: Int, time: Double) {
        withPlayerView(reactTag) { it.seekTo(time) }
    }

    @ReactMethod
    fun setMute(reactTag: Int, mute: Boolean) {
        withPlayerView(reactTag) { it.setMute(mute) }
    }

    @ReactMethod
    fun setPlaybackSpeed(reactTag: Int, playbackSpeed: Double) {
        withPlayerView(reactTag) { it.setPlaybackSpeed(playbackSpeed) }
    }

    @ReactMethod
    fun setScaleMode(reactTag: Int, wantedScaleMode: String) {
        withPlayerView(reactTag) { it.setScaleMode(wantedScaleMode) }
    }

    @ReactMethod
    fun destroy(reactTag: Int) {
        withPlayerView(reactTag) { it.destroy() }
    }
}
