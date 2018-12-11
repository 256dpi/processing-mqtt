/**
 * ##library.name##
 *
 * <p>##library.sentence##
 *
 * <p>##library.url##
 *
 * <p>Copyright ##copyright## ##author##
 *
 * <p>This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * <p>This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * @author ##author##
 * @modified ##date##
 * @version ##library.prettyVersion## (##library.version##)
 */
package mqtt;

import java.lang.reflect.*;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.System;
import java.lang.Throwable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;
import processing.core.*;

class Message {
  String topic;
  MqttMessage message;

  Message(String topic, MqttMessage message) {
    this.topic = topic;
    this.message = message;
  }
}

class Will {
  String topic;
  byte[] payload;
  int qos;
  boolean retained;

  Will(String topic, byte[] payload, int qos, boolean retained) {
    this.topic = topic;
    this.payload = payload;
    this.qos = qos;
    this.retained = retained;
  }
}

/** An MQTTClient that can publish and subscribe. */
public class MQTTClient implements MqttCallbackExtended {
  private MqttAsyncClient client;
  private PApplet parent;
  private CopyOnWriteArrayList<Message> messages;
  private Will will;
  private Method messageReceivedMethod;
  private Method clientConnectedMethod;
  private Method connectionLostMethod;

  /**
   * The constructor, usually called in the setup() method in your sketch to initialize and start
   * the library.
   *
   * @param parent A reference to the running sketch.
   */
  public MQTTClient(PApplet parent) throws Exception {
    // save parent
    this.parent = parent;

    // init messages list
    messages = new CopyOnWriteArrayList<>();

    // register callbacks
    parent.registerMethod("dispose", this);
    parent.registerMethod("draw", this);

    // find callbacks
    messageReceivedMethod = findMessageReceivedCallback();
    clientConnectedMethod = findClientConnectedCallback();
    connectionLostMethod = findConnectionLostCallback();
  }

  /**
   * Set a last will message with topic, payload, QoS and the retained flag.
   *
   * @param topic The topic.
   * @param payload The payload.
   * @param qos The qos level.
   * @param retained The retainged flag.
   */
  public void setWill(String topic, String payload, int qos, boolean retained) {
    setWill(topic, payload.getBytes(Charset.forName("UTF-8")), qos, retained);
  }

  /**
   * Set a last will message with topic and payload.
   *
   * @param topic The topic.
   * @param payload The payload.
   */
  public void setWill(String topic, String payload) {
    setWill(topic, payload, 0, false);
  }

  /**
   * Set a last will message with topic, payload, QoS and the retained flag.
   *
   * @param topic The topic.
   * @param payload The payload.
   * @param qos The qos.
   * @param retained The retained flag.
   */
  public void setWill(String topic, byte[] payload, int qos, boolean retained) {
    will = new Will(topic, payload, qos, retained);
  }

  /**
   * Connect to a broker using only a broker URI.
   *
   * @param brokerURI The broker URI.
   */
  public void connect(String brokerURI) throws Exception {
    connect(brokerURI, MqttClient.generateClientId());
  }

  /**
   * Connect to a broker using an broker URI and client ID.
   *
   * @param brokerURI The broker URI.
   * @param clientID The client ID.
   */
  public void connect(String brokerURI, String clientID) throws Exception {
    connect(brokerURI, clientID, true);
  }

  /**
   * Connect to a broker using an broker URI, client Id and cleanSession flag.
   *
   * @param brokerURI The broker URI.
   * @param clientId The client ID.
   * @param cleanSession The clean session flag.
   */
  public void connect(String brokerURI, String clientId, boolean cleanSession) throws Exception {
    URI uri = null;
    try {
      uri = new URI(brokerURI);
    } catch (URISyntaxException e) {
      throw new Exception("[MQTT] Failed to parse URI: " + e.getMessage(), e);
    }

    try {
      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(cleanSession);
      options.setAutomaticReconnect(true);

      if (will != null) {
        options.setWill(will.topic, will.payload, will.qos, will.retained);
      }

      if (uri.getUserInfo() != null) {
        String[] auth = uri.getUserInfo().split(":");

        if (auth.length > 0) {
          options.setUserName(auth[0]);

          if (auth.length > 1) {
            options.setPassword(auth[1].toCharArray());
          }
        }
      }

      String scheme = uri.getScheme();
      if (scheme.equals("mqtt")) {
        scheme = "tcp";
      } else if (scheme.equals("mqtts")) {
        scheme = "ssl";
      }

      String loc = scheme + "://" + uri.getHost();
      if (uri.getPort() != -1) {
        loc = loc + ":" + uri.getPort();
      }

      client = new MqttAsyncClient(loc, clientId, new MemoryPersistence());
      client.setCallback(this);
      client.connect(options);

      // TODO: Call connected handler.
    } catch (MqttException e) {
      throw new Exception("[MQTT] Failed to connect:: " + e.getMessage(), e);
    }
  }

  /**
   * Publish a message with a topic.
   *
   * @param topic The topic.
   */
  public void publish(String topic) throws Exception {
    byte[] bytes = {};
    publish(topic, bytes);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @param topic The topic.
   * @param payload The payload.
   */
  public void publish(String topic, String payload) throws Exception {
    publish(topic, payload, 0, false);
  }

  /**
   * Publish a message with a topic, payload qos and retain flag.
   *
   * @param topic The topic.
   * @param payload The payload.
   * @param qos The qos level.
   * @param retained The retained flag.
   */
  public void publish(String topic, String payload, int qos, boolean retained) throws Exception {
    publish(topic, payload.getBytes(Charset.forName("UTF-8")), qos, retained);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @param topic The topic.
   * @param payload The payload.
   */
  public void publish(String topic, byte[] payload) throws Exception {
    publish(topic, payload, 0, false);
  }

  /**
   * Publish a message with a topic, payload qos and retain flag.
   *
   * @param topic The topic.
   * @param payload The payload.
   * @param qos The qos level.
   * @param retained The retained flag.
   */
  public void publish(String topic, byte[] payload, int qos, boolean retained) throws Exception {
    try {
      client.publish(topic, payload, qos, retained);
    } catch (MqttException e) {
      throw new Exception("[MQTT] Failed to publish: " + e.getMessage(), e);
    }
  }

  /**
   * Subscribe a topic.
   *
   * @param topic The topic.
   */
  public void subscribe(String topic) throws Exception {
    this.subscribe(topic, 0);
  }

  /**
   * Subscribe a topic with QoS.
   *
   * @param topic The topic.
   * @param qos The qos level.
   */
  public void subscribe(String topic, int qos) throws Exception {
    try {
      client.subscribe(topic, qos);
    } catch (MqttException e) {
      throw new Exception("[MQTT] Failed to subscribe: " + e.getMessage(), e);
    }
  }

  /**
   * Unsubscribe a topic.
   *
   * @param topic The topic.
   */
  public void unsubscribe(String topic) throws Exception {
    try {
      client.unsubscribe(topic);
    } catch (MqttException e) {
      throw new Exception("[MQTT] Failed to unsubscribe: " + e.getMessage(), e);
    }
  }

  /** Disconnect from the broker. */
  public void disconnect() throws Exception {
    try {
      client.disconnect();
    } catch (MqttException e) {
      throw new Exception("[MQTT] Failed to disconnect: " + e.getMessage(), e);
    }
  }

  public void dispose() {
    try {
      disconnect();
    } catch (Exception e) {
      // ignore
    }
  }

  public void draw() throws Exception {
    for (Message message : messages) {
      messageReceivedMethod.invoke(parent, message.topic, message.message.getPayload());
      messages.remove(message);
    }

    messages.clear();
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    if (messageReceivedMethod != null) {
      messages.add(new Message(topic, mqttMessage));
    }
  }

  @Override
  public void connectionLost(Throwable throwable) {
    System.out.println("[MQTT] Lost connection! (" + throwable.getMessage() + ")");
  }

  @Override
  public void connectComplete(boolean b, String s) {
    System.out.println("[MQTT] Got connection! (" + b + ", " + s + " )");
  }

  private Method findMessageReceivedCallback() throws Exception {
    try {
      return parent.getClass().getMethod("messageReceived", String.class, byte[].class);
    } catch (Exception e) {
      throw new Exception("MQTT] Callback not found!", e);
    }
  }

  private Method findClientConnectedCallback() {
    try {
      return parent.getClass().getMethod("clientConnected", boolean.class);
    } catch (Exception e) {
      return null;
    }
  }

  private Method findConnectionLostCallback() {
    try {
      return parent.getClass().getMethod("connectionLost");
    } catch (Exception e) {
      return null;
    }
  }
}
