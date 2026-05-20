package com.newreactnativedailymotionsdk.DailymotionPlayer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.dailymotion.player.android.sdk.Dailymotion
import com.dailymotion.player.android.sdk.Orientation
import com.dailymotion.player.android.sdk.PlayerParameters
import com.dailymotion.player.android.sdk.PlayerView
import com.dailymotion.player.android.sdk.ScaleMode
import com.dailymotion.player.android.sdk.listeners.AdListener
import com.dailymotion.player.android.sdk.listeners.PlayerListener
import com.dailymotion.player.android.sdk.listeners.VideoListener
import com.dailymotion.player.android.sdk.webview.error.PlayerError
import com.dailymotion.player.android.sdk.webview.events.PlayerEvent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.newreactnativedailymotionsdk.player.R

class PlayerParametersBuilder {
    private var customConfig: Map<String, String> = mutableMapOf()
    private var scaleMode: ScaleMode = ScaleMode.Fit
    private var mute: Boolean = false
    private var startTime: Long = 0L
    private var loop: Boolean = false
    private var allowAAID: Boolean = true
    private var defaultFullscreenOrientation: Orientation = Orientation.Landscape

    fun setCustomConfig(config: Map<String, String>) = apply { customConfig = config }
    fun setScaleMode(mode: ScaleMode) = apply { scaleMode = mode }
    fun setMute(value: Boolean) = apply { mute = value }
    fun setStartTime(time: Long) = apply { startTime = time }
    fun setLoop(value: Boolean) = apply { loop = value }
    fun setAllowAAID(value: Boolean) = apply { allowAAID = value }
    fun setDefaultFullscreenOrientation(orientation: Orientation) = apply {
        defaultFullscreenOrientation = orientation
    }

    fun build(): PlayerParameters = PlayerParameters(
        customConfig = customConfig,
        scaleMode = scaleMode,
        mute = mute,
        startTime = startTime,
        loop = loop,
        allowAAID = allowAAID,
        defaultFullscreenOrientation = defaultFullscreenOrientation
    )
}

class DailymotionPlayerNativeView(context: ThemedReactContext?) : FrameLayout(context!!) {
    private var playerId: String = ""
    private var videoId: String = ""
    private var playlistId: String = ""
    private var playerParameters: PlayerParameters = PlayerParametersBuilder().build()
    private var dmPlayer: PlayerView? = null
    private var playerInitialized: Boolean = false

    private fun getReactContext(): ReactContext = context as ReactContext

    init {
        inflate(getReactContext(), R.layout.dm_player_container, this)
    }

    private fun sendEvent(eventName: String, data: WritableMap?) {
        try {
            val reactContext = getReactContext()
            if (!reactContext.hasActiveReactInstance()) {
                Log.w(TAG, "CatalystInstance not active, skipping event: $eventName")
                return
            }
            val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
            if (eventDispatcher != null) {
                val eventData = Arguments.createMap()
                eventData.putString("event", eventName)
                eventData.putInt("target", id)
                data?.let { eventData.merge(it) }
                eventDispatcher.dispatchEvent(DailymotionEvent(id, eventData))
            } else {
                Log.w(TAG, "EventDispatcher is null, skipping event: $eventName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event: $eventName", e)
        }
    }

    override fun requestLayout() {
        super.requestLayout()
        post(measureAndLayout)
    }

    private val measureAndLayout = Runnable {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        layout(left, top, right, bottom)
    }

    private fun loadThePlayer() {
        val currentActivity = getReactContext().currentActivity ?: run {
            Log.e(TAG, "currentActivity is null")
            return
        }

        val playerContainerView = findViewById<FrameLayout>(R.id.playerContainerView) ?: run {
            Log.e(TAG, "playerContainerView not found")
            return
        }

        createDailymotionPlayer(
            context = context,
            playerId = playerId,
            videoId = videoId,
            playlistId = playlistId,
            playerParameters = playerParameters,
            playerContainerView = playerContainerView
        )
    }

    private fun createDailymotionPlayer(
        context: Context,
        playerId: String,
        videoId: String,
        playlistId: String,
        playerParameters: PlayerParameters,
        playerContainerView: FrameLayout
    ) {
        Log.d(TAG, "createDailymotionPlayer: playerId=$playerId videoId=$videoId")

        Dailymotion.createPlayer(
            context,
            playerId = playerId,
            videoId = videoId,
            playlistId = playlistId,
            playerParameters = playerParameters,
            playerSetupListener = object : Dailymotion.PlayerSetupListener {
                override fun onPlayerSetupFailed(error: PlayerError) {
                    Log.e(TAG, "Player setup failed: ${error.message}")
                }

                override fun onPlayerSetupSuccess(player: PlayerView) {
                    dmPlayer = player
                    playerContainerView.addView(
                        dmPlayer,
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    )
                    Log.d(TAG, "Player added to view hierarchy")
                    runTheVideo()
                }
            },
            playerListener = object : PlayerListener {
                override fun onFullscreenRequested(playerDialogFragment: DialogFragment) {
                    super.onFullscreenRequested(playerDialogFragment)
                    val activity = getReactContext().currentActivity as? FragmentActivity
                    activity?.supportFragmentManager?.let {
                        playerDialogFragment.show(it, "dmPlayerFullscreenFragment")
                    }
                    sendEvent("onFullscreenRequested", null)
                }

                override fun onPlayerError(playerView: PlayerView, error: PlayerError) {
                    super.onPlayerStart(playerView)
                    sendEvent("onPlayerError", Arguments.createMap().apply {
                        putString("code", error.code)
                        putString("message", error.message)
                        putString("title", error.title)
                    })
                }

                override fun onPlayerStart(playerView: PlayerView) {
                    super.onPlayerStart(playerView)
                    sendEvent("onPlayerStart", null)
                }

                override fun onPlayerEnd(playerView: PlayerView) {
                    super.onPlayerEnd(playerView)
                    sendEvent("onPlayerEnd", null)
                }

                override fun onPlayerVideoChange(playerView: PlayerView, event: PlayerEvent.VideoChange) {
                    super.onPlayerVideoChange(playerView, event)
                    sendEvent("onPlayerVideoChange", Arguments.createMap().apply {
                        putString("videoId", event.videoId)
                    })
                }

                override fun onPlayerVolumeChange(playerView: PlayerView, volume: Long, muted: Boolean) {
                    super.onPlayerVolumeChange(playerView, volume, muted)
                    sendEvent("onPlayerVolumeChange", Arguments.createMap().apply {
                        putDouble("volume", volume.toDouble())
                        putBoolean("muted", muted)
                    })
                }

                override fun onPlayerPlaybackPermission(playerView: PlayerView, event: PlayerEvent.PlaybackPermission) {
                    super.onPlayerPlaybackPermission(playerView, event)
                    sendEvent("onPlayerPlaybackPermission", Arguments.createMap().apply {
                        putString("reason", event.reason)
                        putString("status", event.status)
                    })
                }

                override fun onPlayerPlaybackSpeedChange(playerView: PlayerView, event: PlayerEvent.PlaybackSpeedChange) {
                    super.onPlayerPlaybackSpeedChange(playerView, event)
                    sendEvent("onPlayerPlaybackSpeedChange", Arguments.createMap().apply {
                        putDouble("speed", event.speed)
                    })
                }

                override fun onPlayerScaleModeChange(playerView: PlayerView, scaleMode: String) {
                    super.onPlayerScaleModeChange(playerView, scaleMode)
                    sendEvent("onPlayerScaleModeChange", Arguments.createMap().apply {
                        putString("scaleMode", scaleMode)
                    })
                }
            },
            videoListener = object : VideoListener {
                override fun onVideoStart(playerView: PlayerView) {
                    super.onVideoStart(playerView)
                    sendEvent("onVideoStart", null)
                }

                override fun onVideoEnd(playerView: PlayerView) {
                    super.onVideoEnd(playerView)
                    sendEvent("onVideoEnd", null)
                }

                override fun onVideoPlay(playerView: PlayerView) {
                    super.onVideoPlay(playerView)
                    sendEvent("onVideoPlay", null)
                }

                override fun onVideoPause(playerView: PlayerView) {
                    super.onVideoPause(playerView)
                    sendEvent("onVideoPause", null)
                }

                override fun onVideoPlaying(playerView: PlayerView) {
                    super.onVideoPlaying(playerView)
                    sendEvent("onVideoPlaying", null)
                }

                override fun onVideoBuffering(playerView: PlayerView) {
                    super.onVideoBuffering(playerView)
                    sendEvent("onVideoBuffering", null)
                }

                override fun onVideoProgress(playerView: PlayerView, time: Long) {
                    super.onVideoProgress(playerView, time)
                    sendEvent("onVideoProgress", Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    })
                }

                override fun onVideoTimeChange(playerView: PlayerView, time: Long) {
                    super.onVideoTimeChange(playerView, time)
                    sendEvent("onVideoTimeChange", Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    })
                }

                override fun onVideoDurationChange(playerView: PlayerView, duration: Long) {
                    super.onVideoDurationChange(playerView, duration)
                    sendEvent("onVideoDurationChange", Arguments.createMap().apply {
                        putDouble("duration", duration.toDouble())
                    })
                }

                override fun onVideoSeekStart(playerView: PlayerView, time: Long) {
                    super.onVideoSeekStart(playerView, time)
                    sendEvent("onVideoSeekStart", Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    })
                }

                override fun onVideoSeekEnd(playerView: PlayerView, time: Long) {
                    super.onVideoSeekEnd(playerView, time)
                    sendEvent("onVideoSeekEnd", Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    })
                }

                override fun onVideoSubtitlesReady(playerView: PlayerView, subtitleList: List<String>) {
                    super.onVideoSubtitlesReady(playerView, subtitleList)
                    sendEvent("onVideoSubtitlesReady", Arguments.createMap().apply {
                        putArray("subtitleList", Arguments.fromList(subtitleList))
                    })
                }

                override fun onVideoSubtitlesChange(playerView: PlayerView, subtitle: String) {
                    super.onVideoSubtitlesChange(playerView, subtitle)
                    sendEvent("onVideoSubtitlesChange", Arguments.createMap().apply {
                        putString("subtitles", subtitle)
                    })
                }

                override fun onVideoQualitiesReady(playerView: PlayerView, qualityList: List<String>) {
                    super.onVideoQualitiesReady(playerView, qualityList)
                    sendEvent("onVideoQualitiesReady", Arguments.createMap().apply {
                        putArray("qualityList", Arguments.fromList(qualityList))
                    })
                }

                override fun onVideoQualityChange(playerView: PlayerView, quality: String) {
                    super.onVideoQualityChange(playerView, quality)
                    sendEvent("onVideoQualityChange", Arguments.createMap().apply {
                        putString("quality", quality)
                    })
                }
            },
            adListener = object : AdListener {
                override fun onAdCompanionsReady(playerView: PlayerView) {
                    super.onAdCompanionsReady(playerView)
                    sendEvent("onAdCompanionsReady", null)
                }

                override fun onAdDurationChange(playerView: PlayerView, duration: Long) {
                    super.onAdDurationChange(playerView, duration)
                    sendEvent("onAdDurationChange", Arguments.createMap().apply {
                        putDouble("duration", duration.toDouble())
                    })
                }

                override fun onAdEnd(playerView: PlayerView, adEnd: PlayerEvent.AdEnd) {
                    super.onAdEnd(playerView, adEnd)
                    sendEvent("onAdEnd", Arguments.createMap().apply {
                        putString("type", adEnd.type)
                        putString("position", adEnd.position)
                        putString("reason", adEnd.reason)
                        putString("error", adEnd.error)
                    })
                }

                override fun onAdPause(playerView: PlayerView) {
                    super.onAdPause(playerView)
                    sendEvent("onAdPause", null)
                }

                override fun onAdPlay(playerView: PlayerView) {
                    super.onAdPlay(playerView)
                    sendEvent("onAdPlay", null)
                }

                override fun onAdStart(playerView: PlayerView, type: String, position: String) {
                    super.onAdStart(playerView, type, position)
                    sendEvent("onAdStart", Arguments.createMap().apply {
                        putString("type", type)
                        putString("position", position)
                    })
                }

                override fun onAdTimeChange(playerView: PlayerView, time: Double) {
                    super.onAdTimeChange(playerView, time)
                    sendEvent("onAdTimeChange", Arguments.createMap().apply {
                        putDouble("time", time)
                    })
                }

                override fun onAdImpression(playerView: PlayerView) {
                    super.onAdImpression(playerView)
                    sendEvent("onAdImpression", null)
                }

                override fun onAdLoaded(playerView: PlayerView, adLoaded: PlayerEvent.AdLoaded) {
                    super.onAdLoaded(playerView, adLoaded)
                    sendEvent("onAdLoaded", Arguments.createMap().apply {
                        putString("position", adLoaded.position)
                        putInt("skipOffset", adLoaded.skipOffset)
                        putBoolean("skippable", adLoaded.skippable)
                        putBoolean("autoplay", adLoaded.autoplay)
                    })
                }

                override fun onAdClick(playerView: PlayerView) {
                    super.onAdClick(playerView)
                    sendEvent("onAdClick", null)
                }

                override fun onAdReadyToFetch(playerView: PlayerView, adReadyToFetch: PlayerEvent.AdReadyToFetch) {
                    super.onAdReadyToFetch(playerView, adReadyToFetch)
                    sendEvent("onAdReadyToFetch", Arguments.createMap().apply {
                        putString("position", adReadyToFetch.position)
                    })
                }
            }
        )
    }

    fun setPlayerId(id: String) { playerId = id }

    fun setVideoId(id: String) { videoId = id }

    fun loadPlayerIfReady() {
        if (playerId.isNotEmpty() && videoId.isNotEmpty() && !playerInitialized) {
            playerInitialized = true
            loadThePlayer()
        }
    }

    fun setPlaylistId(id: String) { playlistId = id }

    fun setPlayerParameters(parameters: ReadableMap) {
        val builder = PlayerParametersBuilder()
        val iterator = parameters.keySetIterator()
        while (iterator.hasNextKey()) {
            when (val key = iterator.nextKey()) {
                "customConfig" -> {
                    val customConfig = mutableMapOf<String, String>()
                    val nested = parameters.getMap(key) ?: continue
                    val nestedIterator = nested.keySetIterator()
                    while (nestedIterator.hasNextKey()) {
                        val nestedKey = nestedIterator.nextKey()
                        if (nested.getType(nestedKey) == ReadableType.String) {
                            customConfig[nestedKey] = nested.getString(nestedKey) ?: ""
                        }
                    }
                    builder.setCustomConfig(customConfig)
                }
                "startTime" -> if (!parameters.isNull(key)) builder.setStartTime(parameters.getDouble(key).toLong())
                "mute" -> if (!parameters.isNull(key)) builder.setMute(parameters.getBoolean(key))
                "loop" -> if (!parameters.isNull(key)) builder.setLoop(parameters.getBoolean(key))
                "scaleMode" -> if (!parameters.isNull(key)) {
                    val mode = when (parameters.getString(key)) {
                        "fit" -> ScaleMode.Fit
                        "fill" -> ScaleMode.Fill
                        "fillLeft" -> ScaleMode.FillLeft
                        "fillRight" -> ScaleMode.FillRight
                        "fillTop" -> ScaleMode.FillTop
                        "fillBottom" -> ScaleMode.FillBottom
                        else -> ScaleMode.Fit
                    }
                    builder.setScaleMode(mode)
                }
                "allowAAID" -> if (!parameters.isNull(key)) builder.setAllowAAID(parameters.getBoolean(key))
                "defaultFullscreenOrientation" -> if (!parameters.isNull(key)) {
                    val orientation = when (parameters.getString(key)) {
                        "landscapeLeft" -> Orientation.Landscape
                        "landscapeRight" -> Orientation.ReverseLandscape
                        "portrait" -> Orientation.Portrait
                        "upsideDown" -> Orientation.ReversePortrait
                        else -> Orientation.Landscape
                    }
                    builder.setDefaultFullscreenOrientation(orientation)
                }
                else -> Log.w(TAG, "Unknown playerParameter key: $key")
            }
        }
        playerParameters = builder.build()
    }

    private fun runTheVideo() {
        dmPlayer?.loadContent(videoId = videoId, playlistId = playlistId, startTime = playerParameters.startTime)
    }

    fun loadContent(videoId: String, playlistId: String?, startTime: Double) {
        dmPlayer?.loadContent(videoId = videoId, playlistId = playlistId, startTime = startTime.toLong())
    }

    fun play() { dmPlayer?.play() }
    fun pause() { dmPlayer?.pause() }

    fun setFullscreen(fullscreen: Boolean, orientationString: String?) {
        val orientation = when (orientationString) {
            "landscapeLeft" -> Orientation.Landscape
            "landscapeRight" -> Orientation.ReverseLandscape
            "portrait" -> Orientation.Portrait
            "upsideDown" -> Orientation.ReversePortrait
            else -> Orientation.Landscape
        }
        dmPlayer?.setFullscreen(fullscreen, orientation)
    }

    fun setSubtitles(subtitle: String) { dmPlayer?.setSubtitles(subtitle) }
    fun setQuality(quality: String) { dmPlayer?.setQuality(quality) }
    fun seekTo(time: Double) { dmPlayer?.seekTo(time.toLong()) }
    fun setMute(mute: Boolean) { dmPlayer?.setMute(mute) }
    fun setPlaybackSpeed(speed: Double) { dmPlayer?.setPlaybackSpeed(speed) }
    fun setScaleMode(scaleMode: String) { dmPlayer?.setScaleMode(scaleMode) }
    fun destroy() { dmPlayer?.destroy() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewRegistry[id] = this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewRegistry.remove(id)
    }

    companion object {
        private const val TAG = "--DailymotionPlayer--"
        val viewRegistry = java.util.concurrent.ConcurrentHashMap<Int, DailymotionPlayerNativeView>()
    }
}
