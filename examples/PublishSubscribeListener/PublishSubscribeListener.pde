// This example sketch connects to shiftr.io
// and sends a message on every keystroke.
//
// After starting the sketch you can find the
// client here: https://shiftr.io/try.
//
// Note: If you're running the sketch via the
// Android Mode you need to set the INTERNET
// permission in Android > Sketch Permissions.
//
// by Joël Gähwiler
// https://github.com/256dpi/processing-mqtt

import mqtt.*;

MQTTClient client;

class Adapter extends MQTTAdapter {
  void clientConnected() {
    println("client connected");

    client.subscribe("/hello");
  }

  void messageReceived(String topic, byte[] payload) {
    println("new message: " + topic + " - " + new String(payload));
  }

  void connectionLost() {
    println("connection lost");
  }
}

Adapter adapter;

void setup() {
  adapter = new Adapter();
  client = new MQTTClient(this, adapter);
  client.connect("mqtt://try:try@broker.shiftr.io", "processing");
}

void draw() {}

void keyPressed() {
  client.publish("/hello", "world");
}
