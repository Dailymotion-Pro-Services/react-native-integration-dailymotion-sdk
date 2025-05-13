import { useEffect, useImperativeHandle, useRef, useState } from 'react';
import { findNodeHandle, NativeModules, NativeSyntheticEvent, requireNativeComponent, StyleProp, ViewStyle } from 'react-native';

type FullscreenOrientation = 'landscapeLeft' | 'landscapeRight' | 'portrait' | 'upsideDown';
type ScaleMode = 'fit' | 'fill' | 'fillLeft' | 'fillRight' | 'fillTop' | 'fillBottom';
type PlaybackSpeed = 0.25 | 0.5 | 0.75 | 1 | 1.25 | 1.5 | 1.75 | 2;

export interface DailymotionPlayerRef {
  loadContent: (videoId: string, playlistId?: string, startTime?: number) => void;
  play: () => void;
  pause: () => void;
  setMute: (mute: boolean) => void;
  setQuality: (quality: string) => void;
  seekTo: (to: number) => void;
  setScaleMode: (mode: ScaleMode) => void;
  setFullscreen: (fullscreen: boolean, orientation: FullscreenOrientation) => void;
  setSubtitles: (code: string) => void;
  setPlaybackSpeed: (speed: PlaybackSpeed) => void;
  destroy: () => void; // Android only
}

interface DailymotionPlayerNativePlayerParameters {
  customConfig?: { [key: string]: string };
  startTime?: number;
  mute?: boolean;
  loop?: boolean;
  scaleMode?: ScaleMode;
  allowAAID?: boolean; // Android only
  allowPIP?: boolean; // iOS only
  allowIDFA?: boolean; // iOS only
  defaultFullscreenOrientation?: FullscreenOrientation;
}

interface DailymotionPlayerProps {
  playerRef?: React.Ref<DailymotionPlayerRef>;
  playerId: string;
  videoId: string;
  playlistId?: string;
  playerParameters?: DailymotionPlayerNativePlayerParameters;
  style?: ViewStyle;
  onEvent?: (event: { event: string;[key: string]: any }) => void;
}

const DailyMotionPlayer = requireNativeComponent<{
  playerId: string;
  videoId: string;
  playlistId?: string;
  playerParameters?: DailymotionPlayerNativePlayerParameters;
  style?: StyleProp<ViewStyle>;
  onEvent: (event: NativeSyntheticEvent<{ event: string;[key: string]: any }>) => void;
}>('DailymotionPlayerNative');

const { DailymotionPlayerNative } = NativeModules;

const DailymotionPlayerComponent = ({
  playerRef,
  playerId,
  videoId,
  playlistId,
  playerParameters,
  style,
  onEvent,
}: DailymotionPlayerProps) => {
  const internalRef = useRef(null);

  const callMethod = async (method: string, ...args: any[]) => {
    if (internalRef.current) {
      const reactTag = findNodeHandle(internalRef.current);
      DailymotionPlayerNative[method](reactTag, ...args);
    }
  };

  useImperativeHandle(playerRef, () => ({
    loadContent: (videoId: string, playlistId?: string, startTime: number = 0) => callMethod('loadContent', videoId, playlistId, startTime),
    play: () => callMethod('play'),
    pause: () => callMethod('pause'),
    setFullscreen: (fullscreen: boolean, orientation: FullscreenOrientation) => callMethod('setFullscreen', fullscreen, orientation),
    setSubtitles: (code: string) => callMethod('setSubtitles', code),
    setQuality: (quality: string) => callMethod('setQuality', quality),
    seekTo: (to: number) => callMethod('seekTo', to),
    setMute: (mute: boolean) => callMethod('setMute', mute),
    setPlaybackSpeed: (speed: PlaybackSpeed) => callMethod('setPlaybackSpeed', speed),
    setScaleMode: (mode: ScaleMode) => callMethod('setScaleMode', mode),
    destroy: () => callMethod('destroy'), // Android only
  }));

  const handleEvent = (event: NativeSyntheticEvent<{ event: string;[key: string]: any }>) => {
    console.log('handleEvent', event.nativeEvent);
    switch (event.nativeEvent.event) {
      case 'playerDidCriticalPathReady': // iOS
      case 'onPlayerStart': // Android
        console.log('playerDidCriticalPathReady/onPlayerStart', event.nativeEvent);
        break;
      case 'didChangePresentationMode': // iOS
      case 'onFullscreenRequested': // Android
        console.log('didChangePresentationMode/onFullscreenRequested', event.nativeEvent);
        break;
      // ... other events 
      // (see https://developers.dailymotion.com/sdk/player-sdk/ios/#events)
      // (see https://developers.dailymotion.com/sdk/player-sdk/android/#events)
      default:
        break;
    }
    if (onEvent) onEvent(event.nativeEvent);
  };

  return (
    <DailyMotionPlayer
      ref={internalRef}
      playerId={playerId}
      videoId={videoId}
      playlistId={playlistId}
      playerParameters={playerParameters}
      style={[{ backgroundColor: 'black' }, style]}
      onEvent={handleEvent}
    />
  );
};

export default DailymotionPlayerComponent;
