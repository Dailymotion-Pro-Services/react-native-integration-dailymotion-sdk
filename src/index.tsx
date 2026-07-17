import { useEffect, useImperativeHandle, useRef } from 'react';
import {
  findNodeHandle,
  NativeModules,
  NativeSyntheticEvent,
  requireNativeComponent,
  StyleProp,
  ViewStyle,
} from 'react-native';

type FullscreenOrientation =
  | 'landscapeLeft'
  | 'landscapeRight'
  | 'portrait'
  | 'upsideDown';
type ScaleMode =
  | 'fit'
  | 'fill'
  | 'fillLeft'
  | 'fillRight'
  | 'fillTop'
  | 'fillBottom';
type PlaybackSpeed = 0.25 | 0.5 | 0.75 | 1 | 1.25 | 1.5 | 1.75 | 2;

export interface DailymotionPlayerRef {
  loadContent: (
    videoId: string,
    playlistId?: string,
    startTime?: number,
  ) => void;
  play: () => void;
  pause: () => void;
  setMute: (mute: boolean) => void;
  setQuality: (quality: string) => void;
  seekTo: (to: number) => void;
  setScaleMode: (mode: ScaleMode) => void;
  setFullscreen: (
    fullscreen: boolean,
    orientation: FullscreenOrientation,
  ) => void;
  setSubtitles: (code: string) => void;
  setPlaybackSpeed: (speed: PlaybackSpeed) => void;
  destroy: () => void;
}

interface DailymotionPlayerNativePlayerParameters {
  customConfig?: { [key: string]: string };
  startTime?: number;
  mute?: boolean;
  loop?: boolean;
  scaleMode?: ScaleMode;
  allowAAID?: boolean;
  defaultFullscreenOrientation?: FullscreenOrientation;
}

export interface DailymotionPlayerProps {
  playerRef?: React.Ref<DailymotionPlayerRef>;
  playerId: string;
  videoId: string;
  playlistId?: string;
  playerParameters?: DailymotionPlayerNativePlayerParameters;
  style?: StyleProp<ViewStyle>;
  onEvent?: (event: { event: string; [key: string]: any }) => void;
}

const DailymotionPlayerNativeComponent = requireNativeComponent<{
  playerId: string;
  videoId: string;
  playlistId?: string;
  playerParameters?: DailymotionPlayerNativePlayerParameters;
  style?: StyleProp<ViewStyle>;
  onEvent: (
    event: NativeSyntheticEvent<{ event: string; [key: string]: any }>,
  ) => void;
}>('DailymotionPlayerNative');

const { DailymotionPlayerNative } = NativeModules;

const DailymotionPlayerView = ({
  playerRef,
  playerId,
  videoId,
  playlistId,
  playerParameters,
  style,
  onEvent,
}: DailymotionPlayerProps) => {
  const internalRef = useRef<any>(null);
  // findNodeHandle returns null once the ref is detached, so the tag is captured
  // while mounted for use in the unmount cleanup below.
  const reactTagRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      // Destroy the native player on unmount so its WebView and ad session don't
      // outlive the view (Android crash in PlayerView.bringAdContainerToFront).
      // Idempotent: the native module no-ops when the view is already gone.
      if (reactTagRef.current != null && DailymotionPlayerNative) {
        DailymotionPlayerNative.destroy(reactTagRef.current);
      }
    };
  }, []);

  const callMethod = (method: string, ...args: any[]) => {
    if (!internalRef.current) return;
    const reactTag = findNodeHandle(internalRef.current);
    if (!DailymotionPlayerNative) {
      console.warn('[DM] NativeModules.DailymotionPlayerNative is undefined');
      return;
    }
    if (reactTag == null) {
      console.warn('[DM] findNodeHandle returned null');
      return;
    }
    DailymotionPlayerNative[method](reactTag, ...args);
  };

  useImperativeHandle(playerRef, () => ({
    loadContent: (
      videoId: string,
      playlistId?: string,
      startTime: number = 0,
    ) => callMethod('loadContent', videoId, playlistId, startTime),
    play: () => callMethod('play'),
    pause: () => callMethod('pause'),
    setFullscreen: (fullscreen: boolean, orientation: FullscreenOrientation) =>
      callMethod('setFullscreen', fullscreen, orientation),
    setSubtitles: (code: string) => callMethod('setSubtitles', code),
    setQuality: (quality: string) => callMethod('setQuality', quality),
    seekTo: (to: number) => callMethod('seekTo', to),
    setMute: (mute: boolean) => callMethod('setMute', mute),
    setPlaybackSpeed: (speed: PlaybackSpeed) =>
      callMethod('setPlaybackSpeed', speed),
    setScaleMode: (mode: ScaleMode) => callMethod('setScaleMode', mode),
    destroy: () => callMethod('destroy'),
  }));

  const handleEvent = (
    event: NativeSyntheticEvent<{ event: string; [key: string]: any }>,
  ) => {
    if (onEvent) onEvent(event.nativeEvent);
  };

  return (
    <DailymotionPlayerNativeComponent
      ref={(node) => {
        internalRef.current = node;
        if (node != null) {
          reactTagRef.current = findNodeHandle(node);
        }
      }}
      playerId={playerId}
      videoId={videoId}
      playlistId={playlistId}
      playerParameters={playerParameters}
      style={style}
      onEvent={handleEvent}
    />
  );
};

export default DailymotionPlayerView;
