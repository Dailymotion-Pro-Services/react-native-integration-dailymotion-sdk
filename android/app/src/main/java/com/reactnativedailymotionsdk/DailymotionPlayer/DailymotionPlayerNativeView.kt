package com.reactnativedailymotionsdk.DailymotionPlayer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.dailymotion.player.android.sdk.Dailymotion
import com.dailymotion.player.android.sdk.Orientation
import com.dailymotion.player.android.sdk.PlayerParameters
import com.dailymotion.player.android.sdk.PlayerView
import com.dailymotion.player.android.sdk.ScaleMode
import com.dailymotion.player.android.sdk.listeners.PlayerListener
import com.dailymotion.player.android.sdk.listeners.VideoListener
import com.dailymotion.player.android.sdk.listeners.AdListener
import com.dailymotion.player.android.sdk.webview.error.PlayerError
import com.dailymotion.player.android.sdk.webview.events.PlayerEvent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.reactnativedailymotionsdk.MainActivity
import com.reactnativedailymotionsdk.R

class PlayerParametersBuilder {
    private var customConfig: Map<String, String> = mutableMapOf()
    private var scaleMode: ScaleMode = ScaleMode.Fit
    private var mute: Boolean = false
    private var startTime: Long = 0L
    private var loop: Boolean = false
    private var allowAAID: Boolean = true
    private var defaultFullscreenOrientation: Orientation = Orientation.Landscape

    fun setCustomConfig(customConfig: Map<String, String>) = apply { this.customConfig = customConfig }
    fun setScaleMode(scaleMode: ScaleMode) = apply { this.scaleMode = scaleMode }
    fun setMute(mute: Boolean) = apply { this.mute = mute }
    fun setStartTime(startTime: Long) = apply { this.startTime = startTime }
    fun setLoop(loop: Boolean) = apply { this.loop = loop }
    fun setAllowAAID(allowAAID: Boolean) = apply { this.allowAAID = allowAAID }
    fun setDefaultFullscreenOrientation(orientation: Orientation) = apply {
        this.defaultFullscreenOrientation = orientation
    }

    fun build(): PlayerParameters {
        return PlayerParameters(
            customConfig = customConfig,
            scaleMode = scaleMode,
            mute = mute,
            startTime = startTime,
            loop = loop,
            allowAAID = allowAAID,
            defaultFullscreenOrientation = defaultFullscreenOrientation
        )
    }
}

class DailymotionPlayerNativeView(context: ThemedReactContext?) : FrameLayout(context!!) {
    private var playerId: String = ""
    private var videoId: String = ""
    private var playlistId: String = ""
    private var playerParameters: PlayerParameters = PlayerParametersBuilder().build()
    private var dmPlayer: PlayerView? = null

    private fun getReactContext(): ReactContext {
        return context as ReactContext
    }

    init {
        inflate(getReactContext(), R.layout.activity_main, this)
    }

    private fun sendEvent(eventName: String, data: WritableMap?) {
        try {
            val reactContext = getReactContext()
            if (!reactContext.hasActiveReactInstance()) {
                Log.w("--DailymotionPlayer--", "CatalystInstance not active, skipping event: $eventName")
                return
            }
            val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
            if (eventDispatcher != null) {
                val eventData = Arguments.createMap()
                eventData.putString("event", eventName)
                eventData.putInt("target", id)
                if (data != null) {
                    eventData.merge(data)
                }
                eventDispatcher.dispatchEvent(DailymotionEvent(id, eventData))
            } else {
                Log.w("--DailymotionPlayer--", "EventDispatcher is null, skipping event: $eventName")
            }
        } catch (e: Exception) {
            Log.e("--DailymotionPlayer--", "Error sending event: $eventName", e)
        }
    }

    override fun requestLayout() {
        super.requestLayout()

        // This view relies on a measure + layout pass happening after it calls requestLayout().
        // https://github.com/facebook/react-native/issues/4990#issuecomment-180415510
        // https://stackoverflow.com/questions/39836356/react-native-resize-custom-ui-component
        post(measureAndLayout)
    }

    private val measureAndLayout = Runnable {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        this.layout(left, top, right, bottom)
    }

    private fun loadThePlayer(): Any {
        val currentActivity = getReactContext().currentActivity

        if (currentActivity != null) {

            // Create and configure the Dailymotion PlayerView
            val playerView = PlayerView(getReactContext())

            val playerContainerView = findViewById<View>(R.id.playerContainerView) as FrameLayout

            if (playerContainerView.layoutParams != null) {
                playerView.layoutParams = playerContainerView.layoutParams
            } else {
                Log.e("--DailymotionPlayer--", "No playerContainerView found")
            }

            return createDailymotionPlayer(
                context,
                playerId = playerId,
                videoId = videoId,
                playlistId = playlistId,
                playerParameters = playerParameters,
                playerContainerView = playerContainerView
            )
        }
        Log.e("--DailymotionPlayer--", "Container null")

        return View(context) as PlayerView
    }

    private fun createDailymotionPlayer(
        context: Context,
        playerId: String,
        videoId: String,
        playlistId: String,
        playerParameters: PlayerParameters,
        playerContainerView: FrameLayout
    ) {

        Log.d("--DailymotionPlayer--", "createDailymotionPlayer")

        Dailymotion.createPlayer(
            context,
            playerId = playerId,
            videoId = videoId,
            playlistId= playlistId,
            playerParameters = playerParameters,
            playerSetupListener =
            object : Dailymotion.PlayerSetupListener {
                override fun onPlayerSetupFailed(error: PlayerError) {
                    Log.e(
                        "--DailymotionPlayer--",
                        "Error while creating Dailymotion player: ${error.message}"
                    )
                }

                override fun onPlayerSetupSuccess(player: PlayerView) {
                    val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    dmPlayer = player
                    playerContainerView.addView(dmPlayer, lp)

                    Log.d(
                        "--DailymotionPlayer--",
                        "Added Dailymotion player $dmPlayer to view hierarchy"
                    )
                    runTheVideo()
                }
            },
            playerListener =
            object : PlayerListener {
                override fun onFullscreenRequested(playerDialogFragment: DialogFragment) {
                    super.onFullscreenRequested(playerDialogFragment)
                    val currentActivity = getReactContext().currentActivity
                    if (currentActivity is MainActivity) {
                        currentActivity.getSupportFragmentManagerInstance()?.let {
                            playerDialogFragment.show(it, "dmPlayerFullscreenFragment")
                        }
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
                    val data = Arguments.createMap().apply {
                        putString("videoId", event.videoId)
                    }
                    sendEvent("onPlayerVideoChange", data)
                }

                override fun onPlayerVolumeChange(
                    playerView: PlayerView,
                    volume: Long,
                    muted: Boolean
                ) {
                    super.onPlayerVolumeChange(playerView, volume, muted)
                    val data = Arguments.createMap().apply {
                        putDouble("volume", volume.toDouble())
                        putBoolean("muted", muted)
                    }
                    sendEvent("onPlayerVolumeChange", data)
                }

                override fun onPlayerPlaybackPermission(
                    playerView: PlayerView,
                    event: PlayerEvent.PlaybackPermission
                ) {
                    super.onPlayerPlaybackPermission(playerView, event)
                    val data = Arguments.createMap().apply {
                        putString("reason", event.reason)
                        putString("status", event.status)
                    }
                    sendEvent("onPlayerPlaybackPermission", data)
                }

                override fun onPlayerPlaybackSpeedChange(
                    playerView: PlayerView,
                    event: PlayerEvent.PlaybackSpeedChange
                ) {
                    super.onPlayerPlaybackSpeedChange(playerView, event)
                    val data = Arguments.createMap().apply {
                        putDouble("speed", event.speed)
                    }
                    sendEvent("onPlayerPlaybackSpeedChange", data)
                }

                override fun onPlayerScaleModeChange(playerView: PlayerView, scaleMode: String) {
                    super.onPlayerScaleModeChange(playerView, scaleMode)
                    val data = Arguments.createMap().apply {
                        putString("scaleMode", scaleMode)
                    }
                    sendEvent("onPlayerScaleModeChange", data)
                }
            },
            videoListener =
            object : VideoListener {
                override fun onVideoSubtitlesChange(playerView: PlayerView, subtitle: String) {
                    super.onVideoSubtitlesChange(playerView, subtitle)
                    val data = Arguments.createMap().apply {
                        putString("subtitles", subtitle)
                    }
                    sendEvent("onVideoSubtitlesChange", data)
                }

                override fun onVideoSubtitlesReady(
                    playerView: PlayerView,
                    subtitleList: List<String>
                ) {
                    super.onVideoSubtitlesReady(playerView, subtitleList)
                    val data = Arguments.createMap().apply {
                        putArray("subtitleList", Arguments.fromList(subtitleList))
                    }
                    sendEvent("onVideoSubtitlesReady", data)
                }

                override fun onVideoDurationChange(playerEvent: PlayerView, duration: Long) {
                    super.onVideoDurationChange(playerEvent, duration)
                    val data = Arguments.createMap().apply {
                        putDouble("duration", duration.toDouble())
                    }
                    sendEvent("onVideoDurationChange", data)
                }

                override fun onVideoEnd(playerView: PlayerView) {
                    super.onVideoEnd(playerView)
                    sendEvent("onVideoEnd", null)
                }

                override fun onVideoPause(playerView: PlayerView) {
                    super.onVideoPause(playerView)
                    sendEvent("onVideoPause", null)
                }

                override fun onVideoPlay(playerView: PlayerView) {
                    super.onVideoPlay(playerView)
                    sendEvent("onVideoPlay", null)
                }

                override fun onVideoPlaying(playerView: PlayerView) {
                    super.onVideoPlaying(playerView)
                    sendEvent("onVideoPlaying", null)
                }

                override fun onVideoProgress(playerEvent: PlayerView, time: Long) {
                    super.onVideoProgress(playerEvent, time)
                    val data = Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    }
                    sendEvent("onVideoProgress", data)
                }

                override fun onVideoQualitiesReady(
                    playerView: PlayerView,
                    qualityList: List<String>
                ) {
                    super.onVideoQualitiesReady(playerView, qualityList)
                    val data = Arguments.createMap().apply {
                        putArray("qualityList", Arguments.fromList(qualityList))
                    }
                    sendEvent("onVideoQualitiesReady", data)
                }

                override fun onVideoQualityChange(playerView: PlayerView, quality: String) {
                    super.onVideoQualityChange(playerView, quality)
                    val data = Arguments.createMap().apply {
                        putString("quality", quality)
                    }
                    sendEvent("onVideoQualityChange", data)
                }

                override fun onVideoSeekEnd(playerView: PlayerView, time: Long) {
                    super.onVideoSeekEnd(playerView, time)
                    val data = Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    }
                    sendEvent("onVideoSeekEnd", data)
                }

                override fun onVideoSeekStart(playerView: PlayerView, time: Long) {
                    super.onVideoSeekStart(playerView, time)
                    val data = Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    }
                    sendEvent("onVideoSeekStart", data)
                }

                override fun onVideoStart(playerView: PlayerView) {
                    super.onVideoStart(playerView)
                    sendEvent("onVideoStart", null)
                }

                override fun onVideoTimeChange(playerView: PlayerView, time: Long) {
                    super.onVideoTimeChange(playerView, time)
                    val data = Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    }
                    sendEvent("onVideoTimeChange", data)
                }

                override fun onVideoBuffering(playerView: PlayerView) {
                    super.onVideoBuffering(playerView)
                    sendEvent("onVideoBuffering", null)
                }
            },
            adListener =
            object : AdListener {
                override fun onAdCompanionsReady(playerView: PlayerView) {
                    super.onAdCompanionsReady(playerView)
                    sendEvent("onAdCompanionsReady", null)
                }

                override fun onAdDurationChange(playerView: PlayerView, duration: Long) {
                    super.onAdDurationChange(playerView, duration)
                    val data = Arguments.createMap().apply {
                        putDouble("duration", duration.toDouble())
                    }
                    sendEvent("onAdDurationChange", data)
                }

                override fun onAdEnd(playerView: PlayerView, adEnd: PlayerEvent.AdEnd) {
                    super.onAdEnd(playerView, adEnd)
                    val data = Arguments.createMap().apply {
                        putString("type", adEnd.type)
                        putString("position", adEnd.position)
                        putString("reason", adEnd.reason)
                        putString("error", adEnd.error)
                    }
                    sendEvent("onAdEnd", data)
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
                    val data = Arguments.createMap().apply {
                        putString("type", type)
                        putString("position", position)
                    }
                    sendEvent("onAdStart", data)
                }

                override fun onAdTimeChange(playerView: PlayerView, time: Double) {
                    super.onAdTimeChange(playerView, time)
                    val data = Arguments.createMap().apply {
                        putDouble("time", time.toDouble())
                    }
                    sendEvent("onAdTimeChange", data)
                }

                override fun onAdImpression(playerView: PlayerView) {
                    super.onAdImpression(playerView)
                    sendEvent("onAdImpression", null)
                }

                override fun onAdLoaded(playerView: PlayerView, adLoaded: PlayerEvent.AdLoaded) {
                    super.onAdLoaded(playerView, adLoaded)
                    val data = Arguments.createMap().apply {
                        putString("position", adLoaded.position)
                        putInt("skipOffset", adLoaded.skipOffset)
                        putBoolean("skippable", adLoaded.skippable)
                        putBoolean("autoplay", adLoaded.autoplay)
                        putArray("verificationScripts", Arguments.fromList(adLoaded.verificationScripts))
                    }
                    sendEvent("onAdLoaded", data)
                }

                override fun onAdClick(playerView: PlayerView) {
                    super.onAdClick(playerView)
                    sendEvent("onAdClick", null)
                }

                override fun onAdReadyToFetch(
                    playerView: PlayerView,
                    adReadyToFetch: PlayerEvent.AdReadyToFetch
                ) {
                    super.onAdReadyToFetch(playerView, adReadyToFetch)
                    val data = Arguments.createMap().apply {
                        putString("position", adReadyToFetch.position)
                    }
                    sendEvent("onAdReadyToFetch", data)
                }
            }
        )
    }

    fun setPlayerId(playerId: String) {
        this.playerId = playerId
        Log.d("--DailymotionPlayer--", "Set player id ${this.playerId}")
        loadThePlayer()
    }

    fun setVideoId(videoId: String) {
        this.videoId = videoId
        Log.d("--DailymotionPlayer--", "Set video id ${this.videoId}")
    }

    fun setPlaylistId(playlistId: String) {
        this.playlistId = playlistId
        Log.d("--DailymotionPlayer--", "Set playlist id ${this.playlistId}")
    }

    fun setPlayerParameters(parameters: ReadableMap) {
        val builder = PlayerParametersBuilder()
        val iterator = parameters.keySetIterator()
        while (iterator.hasNextKey()) {
            when (val key = iterator.nextKey()) {
                "customConfig" -> {
                    val customConfig = mutableMapOf<String, String>()
                    val keySetIterator = parameters.keySetIterator()
                    while (keySetIterator.hasNextKey()) {
                        val keyValue = keySetIterator.nextKey()
                        if (parameters.getType(keyValue) == ReadableType.String) {
                            customConfig[keyValue] = parameters.getString(keyValue) ?: ""
                        } else {
                            Log.w("--DailymotionPlayer--", "Skipping key '$keyValue' because its value is not a string")
                        }
                    }
                    builder.setCustomConfig(customConfig)
                }
                "startTime" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        builder.setStartTime(parameters.getDouble(key).toLong())
                    }
                }
                "mute" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        builder.setMute(parameters.getBoolean(key))
                    }
                }
                "loop" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        builder.setLoop(parameters.getBoolean(key))
                    }
                }
                "scaleMode" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        val scaleMode = when (parameters.getString(key)) {
                            "fit" -> ScaleMode.Fit
                            "fill" -> ScaleMode.Fill
                            "fillLeft" -> ScaleMode.FillLeft
                            "fillRight" -> ScaleMode.FillRight
                            "fillTop" -> ScaleMode.FillTop
                            "fillBottom" -> ScaleMode.FillBottom
                            else -> ScaleMode.Fit
                        }
                        builder.setScaleMode(scaleMode)
                    }
                }
                "allowAAID" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        builder.setAllowAAID(parameters.getBoolean(key))
                    }
                }
                "defaultFullscreenOrientation" -> {
                    if (parameters.hasKey(key) && !parameters.isNull(key)) {
                        val orientation = when (parameters.getString(key)) {
                            "landscapeLeft" -> Orientation.Landscape
                            "landscapeRight" -> Orientation.ReverseLandscape
                            "portrait" -> Orientation.Portrait
                            "upsideDown" -> Orientation.ReversePortrait
                            else -> Orientation.Landscape
                        }
                        builder.setDefaultFullscreenOrientation(orientation)
                    }
                }
                else -> Log.w("--DailymotionPlayer--", "Unknown parameter: $key")
            }
        }
        playerParameters = builder.build()
        Log.d("--DailymotionPlayer--", "Set player parameters: $playerParameters")
    }

    fun runTheVideo() {
        dmPlayer!!.loadContent(videoId=videoId, playlistId=playlistId, startTime=playerParameters.startTime)
    }

    fun loadContent(videoId: String, playlistId: String?, startTime: Double) {
        dmPlayer?.loadContent(videoId, playlistId, startTime.toLong())
    }

    fun play() {
        dmPlayer?.play()
    }

    fun pause() {
        dmPlayer?.pause()
    }

    fun setFullscreen(fullscreen: Boolean, orientationString: String?) {
        val orientationEnum: Orientation = when (orientationString) {
            "landscapeLeft" -> Orientation.Landscape
            "landscapeRight" -> Orientation.ReverseLandscape
            "portrait" -> Orientation.Portrait
            "upsideDown" -> Orientation.ReversePortrait
            else -> Orientation.Landscape
        }
        dmPlayer?.setFullscreen(fullscreen, orientationEnum)
    }

    fun setSubtitles(wantedSubtitle: String) {
        dmPlayer?.setSubtitles(wantedSubtitle)
    }

    fun setQuality(wantedQuality: String) {
        dmPlayer?.setQuality(wantedQuality)
    }

    fun seekTo(time: Double) {
        dmPlayer?.seekTo(time.toLong())
    }

    fun setMute(mute: Boolean) {
        dmPlayer?.setMute(mute)
    }

//    fun setCustomConfig(customConfig: Map<String, String>) {}

    fun setPlaybackSpeed(playbackSpeed: Double) {
        dmPlayer?.setPlaybackSpeed(playbackSpeed)
    }

    fun setScaleMode(wantedScaleMode: String) {
        dmPlayer?.setScaleMode(wantedScaleMode)
    }

//    fun getState(callback: PlayerView.PlayerStateCallback) {
//        dmPlayer?.getState(callback)
//    }

//    fun setLogLevel(logLevel: LogLevel) {}

    fun destroy() {
        dmPlayer?.destroy()
    }
}
