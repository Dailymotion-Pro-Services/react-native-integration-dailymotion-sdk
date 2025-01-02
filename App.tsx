/* eslint-disable react-native/no-inline-styles */
/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  Text,
  View,
  useColorScheme,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';
import DailyMotionPlayer from './DailyMotionPlayer';

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

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
        <View style={{paddingVertical: 10}}>
          <Text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{paddingVertical: 10}}>
          <Text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{paddingVertical: 10}}>
          <Text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>

        <DailyMotionPlayer
          playerId="xtv3w"
          videoId="x8oapzq"
          style={{
            width: '100%',
            height: 300,
          }}
        />
        <View style={{paddingVertical: 10}}>
          <Text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{paddingVertical: 10}}>
          <Text>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
            obcaecati totam quas enim et distinctio earum temporibus, aliquam
            expedita commodi necessitatibus! Molestias possimus fuga unde
            placeat culpa numquam totam perferendis!
          </Text>
        </View>
        <View style={{paddingVertical: 10}}>
          <Text>
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

export default App;
