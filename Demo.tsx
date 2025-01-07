import React from 'react';
import {ScrollView, Text, View} from 'react-native';

import DailyMotionPlayer from './DailyMotionPlayer';

function Demo(): JSX.Element {
  return (
    <ScrollView>
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>

      <DailyMotionPlayer
        playerId="xtv3w"
        videoId="x9by1z0"
        style={{
          width: '100%',
          height: 300,
        }}
      />
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>
      <View style={{paddingVertical: 10}}>
        <Text>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Quos
          obcaecati totam quas enim et distinctio earum temporibus, aliquam
          expedita commodi necessitatibus! Molestias possimus fuga unde placeat
          culpa numquam totam perferendis!
        </Text>
      </View>
    </ScrollView>
  );
}

export default Demo;
