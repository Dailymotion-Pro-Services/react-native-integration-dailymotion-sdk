# react-native-dailymotion-sdk

React Native native UI component wrapping the Dailymotion Player SDK.
Supports **Android** and **iOS** with New Architecture (Fabric interop / bridgeless).

|              | Android                                  | iOS                               |
| ------------ | ---------------------------------------- | --------------------------------- |
| SDK          | Dailymotion Android SDK 2.1.1            | Dailymotion iOS SDK 1.4.11 (SPM)  |
| Architecture | New Architecture (`newArchEnabled=true`) | New Architecture (Fabric interop) |
| Min version  | API 21                                   | iOS 14.0                          |

---

## Requirements

- React Native ≥ 0.76 (New Architecture) — compatible with RN 0.76–0.85+ including Expo SDK 50–54
- Android: API 21+
- iOS: 14.0+, Xcode 14+

---

## Installation

### 1. Add the npm package

```sh
npm install react-native-dailymotion-sdk
# or
yarn add react-native-dailymotion-sdk
```

### 2. Android

Autolinking handles everything. No manual steps.

Add the Dailymotion Maven repository to `android/build.gradle` (root-level `allprojects` block):

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

> `dependencyResolutionManagement` in `settings.gradle` does **not** work — RN's Gradle plugin overrides it. Use `allprojects` instead.

### 3. iOS

**Step 1 — Install pods:**

```sh
cd ios && pod install
```

**Step 2 — Add DailymotionPlayerSDK via Swift Package Manager:**

The iOS SDK is distributed via SPM only. Open your workspace in Xcode:

```sh
open ios/YourApp.xcworkspace
```

Then:

1. **File → Add Package Dependencies...**
2. Enter URL: `https://github.com/dailymotion/player-sdk-ios`
3. When prompted for targets, add `DailymotionPlayerSDK` to **two targets**:
   - Your app target (e.g. `YourApp`)
   - The **`DailymotionPlayer`** pod target (found under the `Pods` project in the left sidebar)

> Adding to the `DailymotionPlayer` pod target is required because the Swift source files are compiled as part of the pod, not the host app.

**Step 3 — Build:**

```sh
npx react-native run-ios
```

---

## Expo / EAS Build

Add the plugin to `app.json`:

```json
{
  "expo": {
    "plugins": ["react-native-dailymotion-sdk"]
  }
}
```

Then run:

```sh
npx expo prebuild
```

No other manual steps required — the plugin automatically configures the Dailymotion Maven repository (Android) and adds the iOS SDK via Swift Package Manager.

### Options

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `iosSdkVersion` | `string` | `"1.4.11"` | Version of `player-sdk-ios` to add via SPM |

```json
["react-native-dailymotion-sdk", { "iosSdkVersion": "1.4.11" }]
```

> **Note:** The iOS SPM package is added to the app target only. The `DailymotionPlayer` pod target resolves it via `FRAMEWORK_SEARCH_PATHS → $(BUILT_PRODUCTS_DIR)/PackageFrameworks` (already set in the podspec). If your EAS Build fails with a `DailymotionPlayerSDK` module-not-found error, open the generated workspace and manually add the package to the `DailymotionPlayer` pod target as described in the iOS section above.

---

## Usage

```tsx
import { useRef } from 'react';
import { View, StyleSheet } from 'react-native';
import DailymotionPlayerView, {
  DailymotionPlayerRef,
} from 'react-native-dailymotion-sdk';

export default function MyScreen() {
  const playerRef = useRef<DailymotionPlayerRef>(null);

  return (
    <View style={styles.container}>
      <DailymotionPlayerView
        playerRef={playerRef}
        playerId="x1kfiw"
        videoId="x6idkj5"
        style={styles.player}
        onEvent={e => console.log(e.event, e)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  player: { width: '100%', aspectRatio: 16 / 9 },
});
```

### Imperative controls

```tsx
// Playback
playerRef.current?.play();
playerRef.current?.pause();
playerRef.current?.setMute(true);
playerRef.current?.seekTo(30); // seconds

// Quality & display
playerRef.current?.setQuality('720'); // '240'|'480'|'720'|'1080'|'default'
playerRef.current?.setScaleMode('fit'); // 'fit'|'fill'|'fillLeft'|'fillRight'|'fillTop'|'fillBottom'
playerRef.current?.setPlaybackSpeed(1.5); // 0.25|0.5|0.75|1|1.25|1.5|1.75|2
playerRef.current?.setSubtitles('en');
playerRef.current?.setFullscreen(true, 'landscapeLeft'); // 'landscapeLeft'|'landscapeRight'|'portrait'|'upsideDown'

// Content
playerRef.current?.loadContent('videoId', 'playlistId?', 0 /* startTime */);
playerRef.current?.destroy();
```

### Sticky / Floating Player

The demo app (`App.tsx`) includes a production-ready sticky player that switches between inline (full-width, scrolls with content) and floating PiP modes. See the full implementation in `App.tsx`.

**Key features:**

- Toggle between inline and sticky modes via button
- Drag player anywhere on screen when sticky
- Resize via bottom-right corner handle
- Single player instance (never unmounts during transitions)
- Smooth GPU-accelerated scroll performance
- Playback continues uninterrupted across mode switches

**Requirements:**

- [`react-native-gesture-handler`](https://docs.swmansion.com/react-native-gesture-handler/) ≥ 2.0
- [`react-native-reanimated`](https://docs.swmansion.com/react-native-reanimated/) ≥ 3.0

```sh
npm install react-native-gesture-handler react-native-reanimated
```

**Architecture:**

```tsx
// Single DailymotionPlayerView, always mounted as absolute overlay
<View pointerEvents="box-none" style={StyleSheet.absoluteFill}>
  <GestureDetector gesture={pan}>
    <Animated.View style={playerAnimatedStyle}>
      <DailymotionPlayerView ... />
    </Animated.View>
  </GestureDetector>
</View>

// Animated style switches between modes:
const playerAnimatedStyle = useAnimatedStyle(() => {
  if (inlineMode) {
    return {
      top: 0,
      left: 0,
      width: screenWidth,
      transform: [{ translateY: -scrollY.value }],  // Follows scroll
    };
  }
  return {
    transform: [{ translateX: x }, { translateY: y }],  // Draggable
    width: stickyWidth,
  };
});
```

Player positioned via `transform` (GPU-accelerated) instead of layout props for smooth 60fps scroll.

> Full implementation with gesture handlers, resize logic, and mode switching available in `App.tsx`.

---

## Props

| Prop               | Type                        | Required | Description                              |
| ------------------ | --------------------------- | -------- | ---------------------------------------- |
| `playerId`         | `string`                    | Yes      | Player ID from the Dailymotion dashboard |
| `videoId`          | `string`                    | Yes      | Video ID to load                         |
| `playlistId`       | `string`                    | No       | Playlist ID                              |
| `playerParameters` | `PlayerParameters`          | No       | Initial player configuration (see below) |
| `onEvent`          | `(e: PlayerEvent) => void`  | No       | Unified event callback                   |
| `style`            | `StyleProp<ViewStyle>`      | No       | Standard RN style                        |
| `playerRef`        | `Ref<DailymotionPlayerRef>` | No       | Imperative handle                        |

### `playerParameters`

```ts
interface PlayerParameters {
  startTime?: number; // seconds
  mute?: boolean;
  loop?: boolean;
  scaleMode?:
    | 'fit'
    | 'fill'
    | 'fillLeft'
    | 'fillRight'
    | 'fillTop'
    | 'fillBottom';
  allowAAID?: boolean; // Android — advertising ID
  allowIDFA?: boolean; // iOS — advertising ID
  defaultFullscreenOrientation?:
    | 'landscapeLeft'
    | 'landscapeRight'
    | 'portrait'
    | 'upsideDown';
  customConfig?: { [key: string]: string };
}
```

---

## Events

All events fire through the single `onEvent` callback. `e.event` is the event name.

### Player events

| Event                                | Extra fields      |
| ------------------------------------ | ----------------- |
| `playerDidStart`                     | —                 |
| `playerDidEnd`                       | —                 |
| `playerDidCriticalPathReady`         | —                 |
| `playerDidChangeVideo`               | `videoId`         |
| `playerDidChangeVolume`              | `volume`, `muted` |
| `playerDidChangeControls`            | `isVisible`       |
| `playerDidChangeScaleMode`           | `scaleMode`       |
| `playerDidChangePresentationMode`    | —                 |
| `playerDidReceivePlaybackPermission` | —                 |
| `playerDidFailWithError`             | `error`           |
| `playerOpenUrl`                      | `url`             |

### Video events

| Event                          | Extra fields    |
| ------------------------------ | --------------- |
| `videoDidStart`                | —               |
| `videoDidEnd`                  | —               |
| `videoDidPlay`                 | —               |
| `videoDidPause`                | —               |
| `videoIsPlaying`               | —               |
| `videoIsBuffering`             | —               |
| `videoDidChangeTime`           | `time`          |
| `videoDidChangeDuration`       | `duration`      |
| `videoDidChangeQuality`        | `quality`       |
| `videoDidReceiveQualitiesList` | `qualities`     |
| `videoDidChangeSubtitles`      | `subtitles`     |
| `videoDidReceiveSubtitlesList` | `subtitlesList` |
| `videoDidSeekStart`            | `time`          |
| `videoDidSeekEnd`              | —               |
| `videoIsInProgress`            | `progressTime`  |

### Ad events

| Event                    | Extra fields       |
| ------------------------ | ------------------ |
| `adDidStart`             | `type`, `position` |
| `adDidEnd`               | —                  |
| `adDidPlay`              | —                  |
| `adDidPause`             | —                  |
| `adDidImpression`        | —                  |
| `adDidClick`             | —                  |
| `adDidLoaded`            | —                  |
| `adDidChangeTime`        | `time`             |
| `adDidChangeDuration`    | `duration`         |
| `adDidReceiveCompanions` | —                  |

---

## Notes

- **Physical device required** — videos time out on emulators/simulators due to network restrictions from the Dailymotion CDN.
- Player IDs are created in the [Dailymotion Partner HQ](https://www.dailymotion.com/partner/x3k4d2/embed/players).
- `destroy()` fully tears down the SDK player instance. Re-mounting the component or calling `loadContent()` after `destroy()` will not work — unmount and remount the component instead.

---

## Demo app

The repository includes a full-featured demo app at `App.tsx`. To run it:

```sh
npm install

# Android
npx react-native run-android

# iOS (after pod install + SPM setup above)
npx react-native run-ios
```

**Demo features:**

- Inline player (scrolls with content)
- Sticky/floating PiP mode with drag & resize
- All playback controls (play/pause/seek/speed/quality)
- Fullscreen support with orientation control
- Event logging
- Video switching

Demo credentials:

```ts
const PLAYER_ID = 'x1kfiw';
const VIDEO_ID = 'x6idkj5';
```

---

## References

- [Dailymotion Android SDK docs](https://developers.dailymotion.com/docs/getting-started-with-the-android-sdk)
- [Dailymotion iOS SDK docs](https://developers.dailymotion.com/docs/getting-started-with-the-ios-sdk)
- [Dailymotion iOS SDK — GitHub](https://github.com/dailymotion/player-sdk-ios)
