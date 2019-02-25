package mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

class Message {
  String topic;
  MqttMessage message;

  Message(String topic, MqttMessage message) {
    this.topic = topic;
    this.message = message;
  }
}
