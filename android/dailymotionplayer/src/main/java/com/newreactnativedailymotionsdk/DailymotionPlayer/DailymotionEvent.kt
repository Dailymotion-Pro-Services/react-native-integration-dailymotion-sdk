package com.newreactnativedailymotionsdk.DailymotionPlayer

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTModernEventEmitter

class DailymotionEvent(
    viewId: Int,
    private val eventData: WritableMap
) : Event<DailymotionEvent>(viewId) {

    companion object {
        const val EVENT_NAME = "onEvent"
    }

    override fun getEventName(): String = EVENT_NAME

    override fun dispatchModern(rctModernEventEmitter: RCTModernEventEmitter) {
        rctModernEventEmitter.receiveEvent(viewTag, eventName, eventData)
    }
}
