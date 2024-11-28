import React, {useState} from 'react';
import {
  Button,
  NativeModules,
  View,
  ActivityIndicator,
  Alert,
  StyleSheet,
} from 'react-native';

const {SendSmsModule} = NativeModules;

export default function App() {
  const [loading, setLoading] = useState(false);

  const sendSms = () => {
    setLoading(true);
    SendSmsModule.sendSms(
      '+1234567890', // Replace with the recipient's phone number
      'This is a test message.',
    )
      .then((response: string) => {
        setLoading(false);
        Alert.alert('Success', 'SMS sent successfully!');
        console.log('Success:', response);
      })
      .catch((error: string) => {
        setLoading(false);
        Alert.alert('Error', 'Failed to send SMS. Please try again.');
        console.error('Error:', error);
      });
  };

  return (
    <View style={styles.container}>
      {loading ? (
        <ActivityIndicator size="large" color="#0000ff" />
      ) : (
        <Button title="Send SMS" onPress={sendSms} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
