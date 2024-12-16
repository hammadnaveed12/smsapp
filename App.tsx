import React, {useState, useEffect} from 'react';
import {
  Button,
  NativeModules,
  View,
  ActivityIndicator,
  DeviceEventEmitter,
  Alert,
  StyleSheet,
  Text,
  Platform,
  PermissionsAndroid,
} from 'react-native';

const {SendSmsModule, IncomingCallModule} = NativeModules;

export default function App() {
  const [loading, setLoading] = useState(false);
  const [incomingNumber, setIncomingNumber] = useState('');

  useEffect(() => {
    requestPermissions()
      .then(granted => {
        if (granted) {
          IncomingCallModule.startListening();
          console.log('Permissions granted');
        } else {
          Alert.alert(
            'Permissions Denied',
            'App needs permissions to detect incoming calls.',
          );
        }
      })
      .catch(err => console.error(err));

    const callListener = DeviceEventEmitter.addListener(
      'IncomingCall',
      number => {
        console.log('Incoming call from:', number);
        setIncomingNumber(number || '');
      },
    );

    return () => {
      callListener.remove();
      IncomingCallModule.stopListening();
    };
  }, []);
  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
          PermissionsAndroid.PERMISSIONS.SEND_SMS,
          PermissionsAndroid.PERMISSIONS.READ_CALL_LOG,
        ]);

        return (
          granted[PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE] &&
          granted[PermissionsAndroid.PERMISSIONS.SEND_SMS] &&
          granted[PermissionsAndroid.PERMISSIONS.READ_CALL_LOG] ===
            PermissionsAndroid.RESULTS.GRANTED
        );
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  const sendSms = () => {
    setLoading(true);
    SendSmsModule.sendSms('+1234567890', 'This is a test message.')
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
      <Text style={{marginTop: 20}}>
        Incoming Call Number: {incomingNumber || 'No call detected yet'}
      </Text>
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
