# processing-mqtt

**paho mqtt library wrapper for processing**

This library bundles the [Java Client](https://eclipse.org/paho/clients/java/) library of the eclipse paho project and adds a thin wrapper to get an Processing like API.

The first release of the library only supports QoS0 and the basic features to get going. In the next releases more of the features will be available.

This library is an alternative to the [Qatja](https://github.com/Qatja/processing) library which only supports a limited set of features.

## Download

[Download version 1.2.2 of the library.](https://github.com/256dpi/processing-mqtt/releases/download/v1.2.2/mqtt.zip)

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

void messageReceived(String topic, byte[] payload) {
  println("new message: " + topic + " - " + new String(payload));
}
```
