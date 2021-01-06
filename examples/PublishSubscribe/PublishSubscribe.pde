// This example sketch connects to the public shiftr.io instance and sends a message on every keystroke.
// After starting the sketch you can find the client here: https://www.shiftr.io/try.
//
// Note: If you're running the sketch via the Android Mode you need to set the INTERNET permission
// in Android > Sketch Permissions.
//
// by Joël Gähwiler
// https://github.com/256dpi/processing-mqtt

import mqtt.*;

MQTTClient client;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://public:public@public.cloud.shiftr.io", "processing");
}

void draw() {}

void keyPressed() {
  client.publish("/hello", "world");
}

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
