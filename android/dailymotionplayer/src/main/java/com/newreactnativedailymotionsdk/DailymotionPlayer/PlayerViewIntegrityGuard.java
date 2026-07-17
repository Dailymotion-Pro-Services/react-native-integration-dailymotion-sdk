package com.newreactnativedailymotionsdk.DailymotionPlayer;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.dailymotion.player.android.sdk.PlayerView;

import java.lang.reflect.Method;

/**
 * Guards against the SDK crash in PlayerView.bringAdContainerToFront$sdk_release
 * (issue #6): on IMA CONTENT_PAUSE_REQUESTED the SDK calls
 * containerView.removeView(webView) followed by containerView.addView(webView, 0).
 * If the WebView is parented to a different (stale) container — e.g. a leaked,
 * never-destroyed player — removeView() silently no-ops and addView() throws
 * "The specified child already has a parent".
 *
 * This helper is written in Java because the SDK accessors are Kotlin `internal`,
 * which compiles to public bytecode that Java may call but Kotlin may not.
 * It depends on SDK 2.1.1 internals (getContainerView$sdk_release /
 * getPlayerWebView survive R8 in that release) and must be re-verified on every
 * SDK version bump; every path is try/catch-wrapped so on a future SDK it
 * degrades to a no-op instead of breaking ads.
 */
final class PlayerViewIntegrityGuard {

    private static final String TAG = "--DailymotionPlayer--";

    private PlayerViewIntegrityGuard() {
    }

    /**
     * Ensures the player WebView is parented to its own SDK container before an
     * ad break re-parents it. Only detaches when the foreign parent is off-window
     * (a zombie hierarchy); a live foreign parent means another working player
     * owns the WebView, where detaching would break playback — log only.
     */
    static void ensureWebViewParentConsistent(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            Object container = invokeNoArg(playerView, "getContainerView$sdk_release");
            if (!(container instanceof ViewGroup)) {
                return;
            }
            Object webView = invokeNoArg(container, "getPlayerWebView");
            if (!(webView instanceof View)) {
                return;
            }
            ViewParent parent = ((View) webView).getParent();
            if (parent == null || parent == container) {
                return;
            }
            if (parent instanceof ViewGroup && !((ViewGroup) parent).isAttachedToWindow()) {
                Log.w(TAG, "Player WebView parented to a detached foreign ViewGroup before ad break; detaching to prevent crash");
                ((ViewGroup) parent).removeView((View) webView);
            } else {
                Log.w(TAG, "Player WebView parented to a live foreign ViewGroup before ad break; leaving attached");
            }
        } catch (Throwable t) {
            Log.d(TAG, "WebView parent consistency check skipped: " + t);
        }
    }

    private static Object invokeNoArg(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
