import { useMemo, useRef, useState } from 'react';
import {
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  useWindowDimensions,
} from 'react-native';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
} from 'react-native-reanimated';
import {
  Gesture,
  GestureDetector,
  GestureHandlerRootView,
} from 'react-native-gesture-handler';
import DailymotionPlayerView, { DailymotionPlayerRef } from './src/index';

const PLAYER_ID = 'x1kfiw';
const DEFAULT_VIDEO_ID = 'x6idkj5';

const ASPECT_RATIO = 16 / 9;
const STICKY_DEFAULT_WIDTH = 280;
const STICKY_MIN_WIDTH = 160;
const STICKY_MAX_WIDTH = 380;

function PlayerDemo() {
  const insets = useSafeAreaInsets();
  const { width: screenWidth } = useWindowDimensions();
  const playerRef = useRef<DailymotionPlayerRef>(null);

  const [videoId, setVideoId] = useState(DEFAULT_VIDEO_ID);
  const [inputVideoId, setInputVideoId] = useState(DEFAULT_VIDEO_ID);
  const [isMuted, setIsMuted] = useState(false);
  const [events, setEvents] = useState<string[]>([]);
  const [stickyMode, setStickyMode] = useState(false);

  const inlinePlayerHeight = screenWidth / ASPECT_RATIO;

  // Scroll tracking for inline player positioning
  const scrollViewRef = useRef<ScrollView>(null);
  const scrollY = useSharedValue(0);

  // Sticky player drag position
  const translateX = useSharedValue(16);
  const translateY = useSharedValue(16);
  const savedX = useSharedValue(16);
  const savedY = useSharedValue(16);

  // Sticky player resize
  const stickyWidth = useSharedValue(STICKY_DEFAULT_WIDTH);
  const savedStickyWidth = useSharedValue(STICKY_DEFAULT_WIDTH);

  // Mode flag for animated style worklet (0=inline, 1=sticky)
  const isStickyShared = useSharedValue(0);

  const pan = useMemo(
    () =>
      Gesture.Pan()
        .enabled(stickyMode)
        .onUpdate(e => {
          translateX.value = savedX.value + e.translationX;
          translateY.value = savedY.value + e.translationY;
        })
        .onEnd(() => {
          savedX.value = translateX.value;
          savedY.value = translateY.value;
        }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [stickyMode],
  );

  const resizePan = useMemo(
    () =>
      Gesture.Pan()
        .enabled(stickyMode)
        .onBegin(() => {
          savedStickyWidth.value = stickyWidth.value;
        })
        .onUpdate(e => {
          stickyWidth.value = Math.min(
            STICKY_MAX_WIDTH,
            Math.max(STICKY_MIN_WIDTH, savedStickyWidth.value + e.translationX),
          );
        })
        .onEnd(() => {
          savedStickyWidth.value = stickyWidth.value;
        }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [stickyMode],
  );

  // Player position styles - switches between inline (full-width, follows scroll)
  // and sticky (floating PiP, draggable) modes
  const playerAnimatedStyle = useAnimatedStyle(() => {
    if (isStickyShared.value === 0) {
      return {
        position: 'absolute' as const,
        left: 0,
        top: 0,
        width: screenWidth,
        height: inlinePlayerHeight,
        borderRadius: 0,
        overflow: 'hidden' as const,
        transform: [{ translateY: -scrollY.value }],
      };
    }
    return {
      position: 'absolute' as const,
      top: 0,
      left: 0,
      transform: [
        { translateX: translateX.value },
        { translateY: translateY.value },
      ],
      width: stickyWidth.value,
      height: stickyWidth.value / ASPECT_RATIO,
      borderRadius: 8,
      overflow: 'hidden' as const,
    };
  });

  const toggleSticky = (next: boolean) => {
    isStickyShared.value = next ? 1 : 0;

    if (!next) {
      // Scroll to top when exiting sticky so inline player is visible
      scrollViewRef.current?.scrollTo({ y: 0, animated: true });
    }

    setStickyMode(next);
  };

  const btn = (label: string, onPress: () => void, active?: boolean) => (
    <TouchableOpacity
      key={label}
      style={[styles.btn, active && styles.btnActive]}
      onPress={onPress}
    >
      <Text style={[styles.btnText, active && styles.btnTextActive]}>
        {label}
      </Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <ScrollView
        ref={scrollViewRef}
        style={styles.scroll}
        contentContainerStyle={[
          styles.content,
          { paddingBottom: insets.bottom + 16 },
        ]}
        onScroll={e => {
          scrollY.value = e.nativeEvent.contentOffset.y;
        }}
        scrollEventThrottle={16}
      >
        {/* Placeholder reserves space in scroll content where inline player overlays */}
        <View
          style={[
            styles.inlinePlayerPlaceholder,
            { height: inlinePlayerHeight },
          ]}
        />

        <Text style={styles.title}>Dailymotion Player Demo</Text>

        {/* Events log */}
        <View style={styles.eventBox}>
          <View style={styles.eventHeader}>
            <Text style={styles.eventLabel}>Events</Text>
            {events.length > 0 && (
              <TouchableOpacity onPress={() => setEvents([])}>
                <Text style={styles.eventClear}>Clear</Text>
              </TouchableOpacity>
            )}
          </View>
          <ScrollView style={styles.eventScroll} nestedScrollEnabled>
            {events.length === 0 ? (
              <Text style={styles.eventEmpty}>—</Text>
            ) : (
              events.map((ev, i) => (
                <Text key={i} style={styles.eventValue}>
                  {ev}
                </Text>
              ))
            )}
          </ScrollView>
        </View>

        {/* Playback */}
        <Text style={styles.sectionTitle}>Playback</Text>
        <View style={styles.row}>
          {btn('▶  Play', () => playerRef.current?.play())}
          {btn('⏸  Pause', () => playerRef.current?.pause())}
          {btn(
            isMuted ? '🔇 Unmute' : '🔊 Mute',
            () => {
              const next = !isMuted;
              setIsMuted(next);
              playerRef.current?.setMute(next);
            },
            isMuted,
          )}
        </View>

        {/* Seek */}
        <Text style={styles.sectionTitle}>Seek</Text>
        <View style={styles.row}>
          {btn('⏮  0s', () => playerRef.current?.seekTo(0))}
          {btn('30s', () => playerRef.current?.seekTo(30))}
          {btn('60s', () => playerRef.current?.seekTo(60))}
          {btn('120s', () => playerRef.current?.seekTo(120))}
        </View>

        {/* Playback speed */}
        <Text style={styles.sectionTitle}>Playback Speed</Text>
        <View style={styles.row}>
          {([0.5, 0.75, 1, 1.25, 1.5, 2] as const).map(speed =>
            btn(`${speed}x`, () => playerRef.current?.setPlaybackSpeed(speed)),
          )}
        </View>

        {/* Quality */}
        <Text style={styles.sectionTitle}>Quality</Text>
        <View style={styles.row}>
          {['240', '480', '720', '1080', 'default'].map(q =>
            btn(q === 'default' ? 'Auto' : `${q}p`, () =>
              playerRef.current?.setQuality(q),
            ),
          )}
        </View>

        {/* Scale mode */}
        <Text style={styles.sectionTitle}>Scale Mode</Text>
        <View style={styles.row}>
          {(['fit', 'fill', 'fillLeft', 'fillRight'] as const).map(mode =>
            btn(mode, () => playerRef.current?.setScaleMode(mode)),
          )}
        </View>

        {/* Fullscreen */}
        <Text style={styles.sectionTitle}>Fullscreen</Text>
        <View style={styles.row}>
          {btn('↗  Enter', () =>
            playerRef.current?.setFullscreen(true, 'landscapeLeft'),
          )}
          {btn('↙  Exit', () =>
            playerRef.current?.setFullscreen(false, 'portrait'),
          )}
        </View>

        {/* Sticky Player */}
        <Text style={styles.sectionTitle}>Sticky Player</Text>
        <Text style={styles.sectionHint}>
          Drag to move · Drag corner to resize
        </Text>
        <View style={styles.row}>
          {btn('▣  Sticky', () => toggleSticky(true), stickyMode)}
          {btn('✕  Exit', () => toggleSticky(false))}
        </View>

        {/* Load different video */}
        <Text style={styles.sectionTitle}>Load Video</Text>
        <View style={styles.inputRow}>
          <TextInput
            style={styles.input}
            value={inputVideoId}
            onChangeText={setInputVideoId}
            placeholder="Video ID"
            placeholderTextColor="#888"
            autoCapitalize="none"
            autoCorrect={false}
          />
          {btn('Load', () => {
            setVideoId(inputVideoId);
            playerRef.current?.loadContent(inputVideoId);
          })}
        </View>

        {/* Destroy */}
        <Text style={styles.sectionTitle}>Lifecycle</Text>
        <View style={styles.row}>
          {btn('💥 Destroy', () => playerRef.current?.destroy())}
        </View>
      </ScrollView>

      {/* Always-mounted player overlay - repositions between inline and sticky modes */}
      <View pointerEvents="box-none" style={StyleSheet.absoluteFill}>
        <GestureDetector gesture={pan}>
          <Animated.View
            style={[
              stickyMode ? styles.stickyElevation : null,
              playerAnimatedStyle,
            ]}
          >
            <DailymotionPlayerView
              playerRef={playerRef}
              playerId={PLAYER_ID}
              videoId={videoId}
              style={{ flex: 1, backgroundColor: 'red' }}
              onEvent={e => setEvents(prev => [e.event, ...prev])}
            />
            {stickyMode && (
              <View style={styles.controlsBar}>
                <Text style={styles.dragHandle}>⠿</Text>
                <TouchableOpacity
                  style={styles.controlBtn}
                  onPress={() => toggleSticky(false)}
                >
                  <Text style={styles.controlBtnText}>✕</Text>
                </TouchableOpacity>
              </View>
            )}
            {stickyMode && (
              <GestureDetector gesture={resizePan}>
                <View style={styles.resizeHandle}>
                  <Text style={styles.resizeHandleIcon}>⤡</Text>
                </View>
              </GestureDetector>
            )}
          </Animated.View>
        </GestureDetector>
      </View>
    </View>
  );
}

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <StatusBar barStyle="dark-content" />
        <PlayerDemo />
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scroll: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    padding: 16,
    gap: 8,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    textAlign: 'center',
    marginBottom: 12,
    color: '#111',
  },
  inlinePlayerPlaceholder: {
    marginTop: -16,
    marginHorizontal: -16,
    backgroundColor: '#000',
  },
  stickyElevation: {
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  eventBox: {
    backgroundColor: '#fff',
    borderRadius: 6,
    padding: 10,
    gap: 6,
  },
  eventHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  eventLabel: {
    fontSize: 12,
    color: '#666',
    fontWeight: '600',
  },
  eventClear: {
    fontSize: 11,
    color: '#0057ff',
    fontWeight: '500',
  },
  eventScroll: {
    maxHeight: 100,
  },
  eventEmpty: {
    fontSize: 12,
    color: '#aaa',
    fontFamily: 'monospace',
  },
  eventValue: {
    fontSize: 12,
    color: '#0057ff',
    fontFamily: 'monospace',
    lineHeight: 18,
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: '600',
    color: '#444',
    marginTop: 8,
    marginBottom: 4,
  },
  sectionHint: {
    fontSize: 11,
    color: '#999',
    marginBottom: 4,
    marginTop: -4,
  },
  row: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  btn: {
    backgroundColor: '#fff',
    borderRadius: 6,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  btnActive: {
    backgroundColor: '#0057ff',
    borderColor: '#0057ff',
  },
  btnText: {
    fontSize: 13,
    fontWeight: '500',
    color: '#222',
  },
  btnTextActive: {
    color: '#fff',
  },
  inputRow: {
    flexDirection: 'row',
    gap: 8,
    alignItems: 'center',
  },
  input: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#ddd',
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 13,
    color: '#111',
  },
  controlsBar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 6,
    paddingVertical: 4,
    gap: 4,
    zIndex: 10,
    backgroundColor: 'rgba(0,0,0,0.45)',
  },
  dragHandle: {
    flex: 1,
    color: 'rgba(255,255,255,0.6)',
    fontSize: 14,
  },
  controlBtn: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.2)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  controlBtnText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
  },
  resizeHandle: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0,0,0,0.35)',
    borderTopLeftRadius: 6,
  },
  resizeHandleIcon: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 14,
  },
});
