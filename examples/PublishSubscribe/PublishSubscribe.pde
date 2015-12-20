// This example sketch connects to shiftr.io
// and sends a message on every kestroke.
//
// After starting the sketch you can find the
// client here: https://shiftr.io/try.
//
// by Joël Gähwiler
// https://github.com/256dpi/processing-mqtt

import mqtt.*;

MQTTClient client;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://try:try@broker.shiftr.io", "processing");
  client.subscribe("/example");
  // client.unsubscribe("/example");
}

void draw() {}

void keyPressed() {
  client.publish("/hello", "world");
}

void messageReceived(String topic, byte[] payload) {
  println("new message: " + topic + " - " + new String(payload));
}
