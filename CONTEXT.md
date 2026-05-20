# Project Context — React Native Dailymotion Player SDK (Android)

> Static documentation for continuity across machines/sessions.
> Last updated: 2026-05-20

---

## What This Is

A **reusable React Native native UI component** (Android-only for now) that wraps the Dailymotion Android SDK `PlayerView`. Ships as a library consumed by a host app.

- Library JS entry: `src/index.tsx`
- Library Android module: `android/dailymotionplayer/`
- Demo host app: `App.tsx` (runs via `npx react-native run-android`)

---

## Key Versions

| Dependency | Version |
|---|---|
| React Native | 0.85.3 |
| Dailymotion Android SDK | 2.1.1 |
| Kotlin | 2.1.20 |
| New Architecture | **enabled** (`newArchEnabled=true`) |
| Hermes | enabled |

---

## Architecture

```
JS: <DailymotionPlayerView ref={playerRef} playerId="x" videoId="y" />
         │ requireNativeComponent('DailymotionPlayerNative')
         ▼
DailymotionPlayerController  (SimpleViewManager)
  - @ReactProp: playerId, videoId, playlistId, playerParameters
  - onAfterUpdateTransaction → loadPlayerIfReady()   ← batches props before init
  - exports onEvent custom direct event
         │ createViewInstance()
         ▼
DailymotionPlayerNativeView  (FrameLayout)
  - inflates dm_player_container.xml → FrameLayout#playerContainerView
  - Dailymotion.createPlayer() → adds PlayerView to container
  - Attaches PlayerListener / VideoListener / AdListener
  - All SDK events → sendEvent("eventName", data) → DailymotionEvent
  - Self-registers in viewRegistry on onAttachedToWindow (keyed by view.id == reactTag)

Imperative ref (play/pause/seekTo/etc.)
  → findNodeHandle(ref) → NativeModules.DailymotionPlayerNative.method(tag, ...)
         ▼
DailymotionPlayerNativeModule
  - withPlayerView(reactTag): looks up DailymotionPlayerNativeView.viewRegistry[tag]
  - runs action on UI thread via UiThreadUtil.runOnUiThread
```

### Why viewRegistry (not UIManagerModule)

`UIManagerModule.addUIBlock` is Old Architecture only. In RN 0.85.x New Architecture (bridgeless), `getNativeModule(UIManagerModule::class.java)` returns **null** — every imperative call silently did nothing.

Fix: views self-register in a `ConcurrentHashMap<Int, DailymotionPlayerNativeView>` on `onAttachedToWindow` using `view.id` (RN sets this to `reactTag` even in Fabric interop). Module resolves views directly from the registry.

---

## File Map

```
NewReactNativeDailymotionSDK/
├── src/
│   └── index.tsx                        ← JS library entry (requireNativeComponent + useImperativeHandle)
├── App.tsx                              ← Demo app (all controls: play/pause/seek/quality/speed/fullscreen/loadContent/destroy)
├── react-native.config.js               ← Autolinking config
├── android/
│   ├── build.gradle                     ← allprojects.repositories includes DM Maven
│   ├── settings.gradle                  ← includes :dailymotionplayer
│   ├── app/
│   │   ├── build.gradle                 ← implementation project(':dailymotionplayer')
│   │   └── src/main/java/.../
│   │       ├── MainApplication.kt       ← registers DailymotionPlayerViewFactory()
│   │       └── MainActivity.kt          ← unchanged (no FragmentManager needed here)
│   └── dailymotionplayer/               ← LIBRARY MODULE
│       ├── build.gradle                 ← sdk:2.1.1, fragment:1.8.9, coroutines, ads, IMA
│       └── src/main/java/.../DailymotionPlayer/
│           ├── DailymotionEvent.kt              ← Event subclass, dispatches via RCTModernEventEmitter
│           ├── DailymotionPlayerController.kt   ← SimpleViewManager, props, event registration
│           ├── DailymotionPlayerNativeView.kt   ← FrameLayout, SDK init, listeners, viewRegistry
│           ├── DailymotionPlayerNativeModule.kt ← ReactContextBaseJavaModule, imperative commands
│           └── DailymotionPlayerViewFactory.kt  ← ReactPackage (registers both ViewManager + Module)
```

---

## SDK Package Paths (v2.1.1 Gotcha)

Docs claim imports changed but **they did NOT**. Actual paths (confirmed by JAR inspection):

```kotlin
import com.dailymotion.player.android.sdk.Dailymotion
import com.dailymotion.player.android.sdk.PlayerView
import com.dailymotion.player.android.sdk.PlayerParameters
import com.dailymotion.player.android.sdk.Orientation
import com.dailymotion.player.android.sdk.ScaleMode
import com.dailymotion.player.android.sdk.listeners.PlayerListener
import com.dailymotion.player.android.sdk.listeners.VideoListener
import com.dailymotion.player.android.sdk.listeners.AdListener
import com.dailymotion.player.android.sdk.webview.error.PlayerError
import com.dailymotion.player.android.sdk.webview.events.PlayerEvent
```

---

## Maven Repository

Dailymotion SDK is NOT on Maven Central. Must declare in **root** `android/build.gradle`:

```gradle
allprojects {
    repositories {
        maven {
            name = "DailymotionMavenRelease"
            url = uri("https://mvn.dailymotion.com/repository/releases/")
        }
    }
}
```

`dependencyResolutionManagement` in `settings.gradle` does NOT work — RN's Gradle plugin overrides it.

---

## Props

| Prop | Type | Notes |
|---|---|---|
| `playerId` | `string` | Required. Player ID from Dailymotion dashboard |
| `videoId` | `string` | Required |
| `playlistId` | `string?` | Optional |
| `playerParameters` | `object?` | customConfig, startTime, mute, loop, scaleMode, allowAAID, defaultFullscreenOrientation |
| `onEvent` | `(e: {event: string, ...}) => void` | Unified event callback |
| `style` | `StyleProp<ViewStyle>` | Standard RN style |
| `playerRef` | `Ref<DailymotionPlayerRef>` | Imperative ref handle |

## Ref Methods

```ts
playerRef.current?.play()
playerRef.current?.pause()
playerRef.current?.setMute(true)
playerRef.current?.seekTo(30)           // seconds
playerRef.current?.setQuality('720')    // '240'|'480'|'720'|'1080'|'default'
playerRef.current?.setScaleMode('fit')  // 'fit'|'fill'|'fillLeft'|'fillRight'|'fillTop'|'fillBottom'
playerRef.current?.setFullscreen(true, 'landscapeLeft')
playerRef.current?.setSubtitles('en')
playerRef.current?.setPlaybackSpeed(1.5)
playerRef.current?.loadContent('videoId', 'playlistId?', 0)
playerRef.current?.destroy()
```

---

## Known Issues / Decisions

### Props race condition (RESOLVED)
`setPlayerId()` originally called `loadThePlayer()` immediately. If `videoId` prop arrived later (React batching not guaranteed), SDK init fired with empty videoId. Fix: all setters just store values; `loadPlayerIfReady()` is called from `onAfterUpdateTransaction` which fires after all prop updates for a transaction.

### `playerInitialized` flag
Prevents re-creating the player if parent re-renders cause props to be re-set with same values.

### Fullscreen
Uses `(currentActivity as? FragmentActivity)?.supportFragmentManager` — no MainActivity coupling. Works as long as host activity extends `FragmentActivity` (all RN apps do).

### Namespace separation
- App namespace: `com.newreactnativedailymotionsdk`
- Library namespace: `com.newreactnativedailymotionsdk.player`
- Library R import: `import com.newreactnativedailymotionsdk.player.R`

### New Architecture imperative commands (RESOLVED)
See "Why viewRegistry" section above.

---

## Build & Run

```bash
# First time
npm install
cd android && ./gradlew assembleDebug   # verify deps resolve

# Run on device
npx react-native run-android

# Logcat filter
adb logcat | grep -E "(--DailymotionPlayer--|--DailymotionModule--)"
```

---

## Demo App Credentials

```ts
const PLAYER_ID = 'x1kfiw';
const DEFAULT_VIDEO_ID = 'x6idkj5';
```

Player ID must exist in your Dailymotion account. Videos won't load on emulator (network timeout) — use physical device.

---

## Reference

- Old working integration (SDK v1.2.7, RN 0.76.5): `react-native-integration-dailymotion-sdk-2/`
- Dailymotion Android SDK docs: https://developers.dailymotion.com/docs/getting-started-with-the-android-sdk
- GitHub remote: `git@github.com:Dailymotion-Pro-Services/react-native-integration-dailymotion-sdk.git`
