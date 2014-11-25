# processing-mqtt

**paho mqtt library wrapper for processing**

## Download

[Download version 1.0.0 of the library.](https://github.com/256dpi/processing-mqtt/releases/download/v1.0.0/mqtt.zip)

## Example

```java
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
```