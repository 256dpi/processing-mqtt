# processing-mqtt

**MQTT library for Processing based on the Eclipse Paho project**

This library bundles the [Java Client](https://eclipse.org/paho/clients/java/) library of the Eclipse Paho project and adds a thin wrapper to get a Processing like API.

[Download version 1.6.1 of the library.](https://github.com/256dpi/processing-mqtt/releases/download/v1.6.1/mqtt.zip)

*Or even better use the Library Manager in the Processing IDE.*

## Example

This example sketch connects to shiftr.io and sends a message on every keystroke. After starting the sketch you can find the client here: <https://shiftr.io/try>.

```java
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
```

## API

Instantiate a new client by supplying the parent applet:

```java
MQTTClient client = new MQTTClient(PApplet parent);
```

- The constructor expects the following method to be declared on the parent applet: `void messageReceived(String topic, byte[] payload)`. That callback will then be invoked in the future with incoming messages.

Set the will message that gets transmitted to the server in all subsequent connect commands:

```java
void client.setWill(String topic, String payload);
void client.setWill(String topic, String payload, int qos, boolean retained);
```

- The QoS level and retained flag default to `0` and `false` respectively.

Connect to the supplied broker by parsing the URL and setting the optionally supplied client id and clean session flag:

```java
void client.connect(String brokerURI);
void client.connect(String brokerURI, String clientId);
void client.connect(String brokerURI, String clientId, boolean cleanSession);
```

- A client id will be generated if needed and the clean session flag defaults to `true`.

Publish a message to the broker using the supplied topic and the optional payload in form of a String or byte-array. If available it will set the QoS level as well as the retained flag appropriately.

```java
void client.publish(String topic);
void client.publish(String topic, String payload);
void client.publish(String topic, String payload, int qos, boolean retained);
void client.publish(String topic, byte[] payload);
void client.publish(String topic, byte[] payload, int qos, boolean retained);
```

- The QoS level and the retained flag default to `0` and `false` respectively.

Subscribe to the supplied topic using the optionally provided QoS level that defaults to `0`:

```java
void client.subscribe(String topic);
void client.subscribe(String topic, int qos);
```

Unsubscribe from the supplied topic:

```java
void client.unsubscribe(String topic);
```

Disconnect from the broker:

```java
void client.disconnect();
```

## Notes

- If you're running the sketch via the Android Mode you need to set the `INTERNET` permission in `Android > Sketch Permissions`.
