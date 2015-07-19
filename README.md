# processing-mqtt

**paho mqtt library wrapper for processing**

This library bundles the [Java Client](https://eclipse.org/paho/clients/java/) library of the eclipse Paho project and adds a thin wrapper to get a Processing like API.

The library is an alternative to the [Qatja](https://github.com/Qatja/processing) library which only supports a limited set of features.

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

## API

- **`new MQTTClient(PApplet parent)`**

Instantiates a new client by supplying the parent applet. The constructor expects the following method to be declared on the parent applet: `void messageReceived(String topic, byte[] payload)`. That callback will then be invoked in the future with incoming messages.

- **`MQTTClient#setWill(String topic, String payload)`**
- **`MQTTClient#setWill(String topic, String payload, int qos, boolean retained)`**

Sets the will message that gets transmitted to the server in all subsequent connect commands. For more details, see the `MQTTClient#publish` method. The QoS level and retained flag default to `0` and `false` respectively.

- **`MQTTClient#connect(String brokerURI)`**
- **`MQTTClient#connect(String brokerURI, String clientId)`**
- **`MQTTClient#connect(String brokerURI, String clientId, boolean cleanSession)`**

Connects to the supplied broker by parsing the URL and setting the optionally supplied client id and clean session flag. A client id will be generated if needed and the clean session flag defaults to `true`.

- **`MQTTClient#publish(String topic)`**
- **`MQTTClient#publish(String topic, String payload)`**
- **`MQTTClient#publish(String topic, byte[] payload)`**
- **`MQTTClient#publish(String topic, byte[] payload, int qos, boolean retained)`**

Publishes a message to the broker using the supplied topic and the optional payload in form of a String or byte-array. If available it will set the QoS level as well as the retained flag appropriately. The QoS level and the retained flag default to `0` and `false` respectively.

- **`MQTTClient#subscribe(String topic)`**
- **`MQTTClient#subscribe(String topic, int qos)`**

Subscribes to the supplied topic using the optionally provided QoS level. The QoS level defaults to `0`.

- **`MQTTClient#unsubscribe(String topic)`**

Unsubscribes from the supplied topic.

- **`MQTTClient#disconnect()`**

Disconnects from the broker.
