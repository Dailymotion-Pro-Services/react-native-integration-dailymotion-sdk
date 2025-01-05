import React from 'react';
import {
  Button,
  Platform,
  SafeAreaView,
  ScrollView,
  StatusBar,
  Text,
  useColorScheme,
  View
} from 'react-native';

import { Colors } from 'react-native/Libraries/NewAppScreen';
import DailymotionPlayerComponent, { DailymotionPlayerRef } from './DailyMotionPlayer';

function Demo(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const playerRef = React.useRef<DailymotionPlayerRef>(null);

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView
      style={{
        display: 'flex',
        height: '100%',
      }}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView>
        <View style={{ paddingVertical: 10 }}>
          <Text style={{ textAlign: 'center' }}>
            You can control the player using the buttons below. The
            player will be loaded with a (loop: true) video and will start playing
            automatically at 10 seconds with sound muted. (see playerParameters)
          </Text>
        </View>
        <View style={{ paddingVertical: 10 }}>
          <Text style={{ textAlign: 'center', fontWeight: 'bold' }}>
            Known issues with Android :{'\n'}
          </Text>
          <Text style={{ textAlign: 'left', paddingHorizontal: 10 }}>
            {'\u2022'} Very low sound from video (regardless of Android simulator volume){'\n'}
            {'\u2022'} On quality changes video get out of container (seems like View is not redrawing somehow){'\n'}
            {'\u2022'} After loading video (using loadContent, not normal load), play/pause button is not working{'\n'}
            {'\u2022'} Scroll in context menu when in Scroll/Flat/Flashlist trigger scroll from list not from context menu (maybe related to nestedScroll from Android?){'\n'}
            {'\u2022'} When entering fullscreen, video pause/unpause some milliseconds{'\n'}
            {'\u2022'} When user exit fullscreen scroll moves{'\n'}
            {'\u2022'} When user exit upsideDown fullscreen video get out of container{'\n'}
          </Text>
        </View>
        <View style={{ borderBottomColor: 'gray', borderBottomWidth: 1, marginHorizontal: '20%' }} />
        <View style={{ paddingVertical: 10, flexDirection: 'column', rowGap: 10 }}>
          <Button onPress={() => playerRef.current?.loadContent('x4w0q6c', undefined, 10)} title="Load video id x4w0q6c with 10s start time" />
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.play()} title="Play" />
            <Button onPress={() => playerRef.current?.pause()} title="Pause" />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.setFullscreen(true, "portrait")} title="P" />
            <Button onPress={() => playerRef.current?.setFullscreen(true, "landscapeLeft")} title="LL" />
            <Button onPress={() => playerRef.current?.setFullscreen(true, "landscapeRight")} title="LR" />
            <Button onPress={() => playerRef.current?.setFullscreen(true, "upsideDown")} title="UD" />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.setSubtitles('en-auto')} title="English subtitles (Auto)" />
            <Button onPress={() => playerRef.current?.setSubtitles('off')} title="Off" />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            {/* Dunno how to set "Auto" */}
            <Button onPress={() => playerRef.current?.setQuality('auto')} title="Auto" disabled />
            <Button onPress={() => playerRef.current?.setQuality('1080')} title="1080p" />
            <Button onPress={() => playerRef.current?.setQuality('720')} title="720p" />
            <Button onPress={() => playerRef.current?.setQuality('480')} title="480p" />
            <Button onPress={() => playerRef.current?.setQuality('380')} title="380p" />
            <Button onPress={() => playerRef.current?.setQuality('240')} title="240p" />
          </View>
          <Button onPress={() => playerRef.current?.seekTo(30.0)} title="Seek to 30s" />
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.setMute(true)} title="Mute" />
            <Button onPress={() => playerRef.current?.setMute(false)} title="Unmute" />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.setPlaybackSpeed(0.5)} title="0.5x" />
            <Button onPress={() => playerRef.current?.setPlaybackSpeed(1)} title="1x" />
            <Button onPress={() => playerRef.current?.setPlaybackSpeed(1.5)} title="1.5x" />
            <Button onPress={() => playerRef.current?.setPlaybackSpeed(1.75)} title="1.75x" />
            <Button onPress={() => playerRef.current?.setPlaybackSpeed(2)} title="2x" />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-around' }}>
            <Button onPress={() => playerRef.current?.setScaleMode('fit')} title="Fit" />
            <Button onPress={() => playerRef.current?.setScaleMode('fill')} title="Fill" />
            <Button onPress={() => playerRef.current?.setScaleMode('fillLeft')} title="Fill Left" />
            <Button onPress={() => playerRef.current?.setScaleMode('fillRight')} title="Fill Right" />
            <Button onPress={() => playerRef.current?.setScaleMode('fillTop')} title="Fill Top" />
            <Button onPress={() => playerRef.current?.setScaleMode('fillBottom')} title="Fill Bottom" />
          </View>
          <Button onPress={() => playerRef.current?.destroy()} title="Destroy (Android only)" disabled={Platform.OS !== 'android'} />
        </View>
        <DailymotionPlayerComponent
          playerRef={playerRef}
          playerId="xtv3w"
          videoId="x8oapzq"
          playerParameters={{
            startTime: 10,
            mute: true,
            loop: true,
            scaleMode: 'fit',
            defaultFullscreenOrientation: 'portrait',
            allowAAID: false,
            allowIDFA: false,
            allowPIP: true,
            customConfig: {
              'dynamiciu': 'USERID/12345',
              // ...
            },
          }}
          style={{
            width: '100%',
            height: 300,
          }}
        />
        <View style={{ paddingVertical: 10 }}>
          <Text style={{ textAlign: 'center' }}>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{ paddingVertical: 10 }}>
          <Text style={{ textAlign: 'center' }}>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{ paddingVertical: 10 }}>
          <Text style={{ textAlign: 'center' }}>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default Demo;
