# processing-mqtt

**paho mqtt library wrapper for processing**

This library bundles the [Java Client](https://eclipse.org/paho/clients/java/) library of the eclipse Paho project and adds a thin wrapper to get an Processing like API.

This library is an alternative to the [Qatja](https://github.com/Qatja/processing) library which only supports a limited set of features.

## Download

[Download version 1.3.0 of the library.](https://github.com/256dpi/processing-mqtt/releases/download/v1.3.0/mqtt.zip)

## Example

```java
import processing.mqtt.*;

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
```
