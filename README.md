# processing-mqtt

**paho mqtt library wrapper for processing**

This library bundles the [Java Client](https://eclipse.org/paho/clients/java/) library of the eclipse Paho project and adds a thin wrapper to get a Processing like API.

The library is an alternative to the [Qatja](https://github.com/Qatja/processing) library which only supports a limited set of features.

## Download

[Download version 1.4.0 of the library.](https://github.com/256dpi/processing-mqtt/releases/download/v1.4.0/mqtt.zip)

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

Instantiate a new client by supplying the parent applet:

```java
MQTTClient client = new MQTTClient(PApplet parent);
```

- The constructor expects the following method to be declared on the parent applet: `void messageReceived(String topic, byte[] payload)`. That callback will then be invoked in the future with incoming messages.

Set the will message that gets transmitted to the server in all subsequent connect commands:

```java
client.setWill(String topic, String payload);
client.setWill(String topic, String payload, int qos, boolean retained);
```

- The QoS level and retained flag default to `0` and `false` respectively.

Connect to the supplied broker by parsing the URL and setting the optionally supplied client id and clean session flag:

```java
client.connect(String brokerURI);
client.connect(String brokerURI, String clientId);
client.connect(String brokerURI, String clientId, boolean cleanSession);
```

- A client id will be generated if needed and the clean session flag defaults to `true`.

Publish a message to the broker using the supplied topic and the optional payload in form of a String or byte-array. If available it will set the QoS level as well as the retained flag appropriately.

```java
client.publish(String topic);
client.publish(String topic, String payload);
client.publish(String topic, String payload, int qos, boolean retained);
client.publish(String topic, byte[] payload);
client.publish(String topic, byte[] payload, int qos, boolean retained);
```

- The QoS level and the retained flag default to `0` and `false` respectively.

Subscribe to the supplied topic using the optionally provided QoS level that defaults to `0`:

```java
client.subscribe(String topic);
client.subscribe(String topic, int qos);
```

Unsubscribe from the supplied topic:

```java
client.unsubscribe(String topic);
```

Disconnect from the broker:

```java
client.disconnect();
```
