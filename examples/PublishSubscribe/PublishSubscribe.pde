import processing.mqtt.*;

MQTTClient client;

void setup() {
  client = new MQTTClient(this);
  client.connect("mqtt://demo:demo@connect.shiftr.io", "my-client");
  client.subscribe("/hello");
  // client.unsubscribe("/hello");
}

void draw() {}

void keyPressed() {
  client.publish("/hello", "world");
}

void messageReceived(String topic, String payload) {
  println("new message: " + topic + " - " + payload);
}
