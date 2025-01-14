package com.reactnativedailymotionsdk.DailymotionPlayer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.UIManagerModule

class DailymotionPlayerNativeModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "DailymotionPlayerNative"
    }

    private fun withPlayerView(reactTag: Int, action: (DailymotionPlayerNativeView) -> Unit) {
        val uiManager = reactApplicationContext.getNativeModule(UIManagerModule::class.java)
        uiManager?.addUIBlock { nativeViewHierarchyManager ->
            val playerView = nativeViewHierarchyManager.resolveView(reactTag) as DailymotionPlayerNativeView
            action(playerView)
        }
    }

    @ReactMethod
    fun loadContent(reactTag: Int, videoId: String, playlistId: String?, startTime: Double) {
        withPlayerView(reactTag) { playerView ->
            playerView.loadContent(videoId, playlistId, startTime)
        }
    }

    @ReactMethod
    fun play(reactTag: Int) {
        withPlayerView(reactTag) { playerView ->
            playerView.play()
        }
    }

    @ReactMethod
    fun pause(reactTag: Int) {
        withPlayerView(reactTag) { playerView ->
            playerView.pause()
        }
    }

    @ReactMethod
    fun setFullscreen(reactTag: Int, fullscreen: Boolean, orientation: String) {
        withPlayerView(reactTag) { playerView ->
            playerView.setFullscreen(fullscreen, orientation)
        }
    }

    @ReactMethod
    fun setSubtitles(reactTag: Int, wantedSubtitle: String) {
        withPlayerView(reactTag) { playerView ->
            playerView.setSubtitles(wantedSubtitle)
        }
    }
    
    @ReactMethod
    fun setQuality(reactTag: Int, wantedQuality: String) {
        withPlayerView(reactTag) { playerView ->
            playerView.setQuality(wantedQuality)
        }
    }

    @ReactMethod
    fun seekTo(reactTag: Int, time: Double) {
        withPlayerView(reactTag) { playerView ->
            playerView.seekTo(time)
        }
    }

    @ReactMethod
    fun setMute(reactTag: Int, mute: Boolean) {
        withPlayerView(reactTag) { playerView ->
            playerView.setMute(mute)
        }
    }

//     @ReactMethod
//     fun setCustomConfig(reactTag: Int, customConfig: Map<String, String>) {
//         withPlayerView(reactTag) { playerView ->
//             playerView.setCustomConfig(customConfig)
//         }
//     }

    @ReactMethod
    fun setPlaybackSpeed(reactTag: Int, playbackSpeed: Double) {
        withPlayerView(reactTag) { playerView ->
            playerView.setPlaybackSpeed(playbackSpeed)
        }
    }

    @ReactMethod
    fun setScaleMode(reactTag: Int, wantedScaleMode: String) {
        withPlayerView(reactTag) { playerView ->
            playerView.setScaleMode(wantedScaleMode)
        }
    }

//     @ReactMethod
//     fun getState(reactTag: Int, callback: PlayerView.PlayerStateCallback) {
//         withPlayerView(reactTag) { playerView ->
//             playerView.getState(callback)
//         }
//     }

//     @ReactMethod
//     fun setLogLevel(reactTag: Int, logLevel: LogLevel) {
//         withPlayerView(reactTag) { playerView ->
//             playerView.setLogLevel(logLevel)
//         }
//     }

    @ReactMethod
    fun destroy(reactTag: Int) {
        withPlayerView(reactTag) { playerView ->
            playerView.destroy()
        }
    }
}
