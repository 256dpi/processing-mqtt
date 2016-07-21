/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package mqtt;

import java.lang.*;
import java.lang.Class;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import processing.core.PApplet;

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

/**
 * An MQTTClient that can publish and subscribe.
 *
 * @example PublishSubscribe
 */

public class MQTTClient implements MqttCallback {
  PApplet parent;

  CopyOnWriteArrayList<Message> messages;
  Will will;

  Method messageReceivedMethod;

  public MqttClient client;

  /**
   * The constructor, usually called in the setup() method in your sketch to
   * initialize and start the library.
   *
   * @example PublishSubscribe
   * @param parent
   */
  public MQTTClient(PApplet parent) {
    this.parent = parent;
    messages = new CopyOnWriteArrayList<Message>();
    parent.registerMethod("dispose", this);
    parent.registerMethod("draw", this);
    messageReceivedMethod = findCallback("messageReceived");
    System.out.println("##library.name## ##library.prettyVersion## by ##author##");
  }

  /**
   * Set a last will message with topic, payload, QoS and the retained flag.
   *
   * @param topic
   * @param payload
   * @param qos
   * @param retained
   */
  public void setWill(String topic, String payload, int qos, boolean retained) {
    setWill(topic, payload.getBytes(Charset.forName("UTF-8")), qos, retained);
  }

  /**
   * Set a last will message with topic and payload.
   *
   * @param topic
   * @param payload
   */
  public void setWill(String topic, String payload) {
    setWill(topic, payload, 0, false);
  }

  /**
   * Set a last will message with topic, payload, QoS and the retained flag.
   *
   * @param topic
   * @param payload
   * @param qos
   * @param retained
   */
  public void setWill(String topic, byte[] payload, int qos, boolean retained) {
    will = new Will(topic, payload, qos, retained);
  }

  /**
   * Connect to a broker using only a broker URI.
   *
   * @param brokerURI
   */
  public void connect(String brokerURI) {
    connect(brokerURI, MqttClient.generateClientId());
  }

  /**
   * Connect to a broker using an broker URI and client ID.
   *
   * @param brokerURI
   * @param clientID
   */
  public void connect(String brokerURI, String clientID) {
    connect(brokerURI, clientID, true);
  }

  /**
   * Connect to a broker using an broker URI, client Id and cleanSession flag.
   *
   * @example PublishSubscribe
   * @param brokerURI
   * @param clientId
   * @param cleanSession
   */
  public void connect(String brokerURI, String clientId, boolean cleanSession) {
    URI uri = null;
    try {
      uri = new URI(brokerURI);
    } catch(URISyntaxException e) {
      System.out.println("[MQTT] failed to parse URI: " + e.getMessage());
    }

    try {
      MqttConnectOptions options = new MqttConnectOptions();

      options.setCleanSession(cleanSession);

      if(will != null) {
        options.setWill(will.topic, will.payload, will.qos, will.retained);
      }

      if (uri.getUserInfo() != null) {
        String[] auth = uri.getUserInfo().split(":");

        if(auth.length > 0) {
          options.setUserName(auth[0]);

          if(auth.length > 1) {
            options.setPassword(auth[1].toCharArray());
          }
        }
      }

      if (uri.getPort()!=-1){
        client = new MqttClient("tcp://" + uri.getHost() + ":" + uri.getPort(), clientId, new MemoryPersistence());
      } else {
        client = new MqttClient("tcp://" + uri.getHost(), clientId, new MemoryPersistence());
      }

      client.setCallback(this);
      client.connect(options);

      System.out.println("[MQTT] connected to: " + uri.getHost());
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to connect: " + e.getMessage());
    }
  }

  /**
   * Publish a message with a topic.
   *
   * @param topic
   */
  public void publish(String topic) {
    byte[] bytes = {};
    publish(topic, bytes);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @example PublishSubscribe
   * @param topic
   * @param payload
   */
  public void publish(String topic, String payload) {
    publish(topic, payload, 0, false);
  }

  /**
   * Publish a message with a topic, payload qos and retain flag.
   *
   * @param topic
   * @param payload
   * @param qos
   * @param retained
   */
  public void publish(String topic, String payload, int qos, boolean retained) {
    publish(topic, payload.getBytes(Charset.forName("UTF-8")), qos, retained);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @param topic
   * @param payload
   */
  public void publish(String topic, byte[] payload) {
    publish(topic, payload, 0, false);
  }

  /**
   * Publish a message with a topic, payload qos and retain flag.
   *
   * @param topic
   * @param payload
   * @param qos
   * @param retained
   */
  public void publish(String topic, byte[] payload, int qos, boolean retained) {
    try {
      client.publish(topic, payload, qos, retained);
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to publish: " + e.getMessage());
    }
  }

  /**
   * Subscribe a topic.
   *
   * @example PublishSubscribe
   * @param topic
   */
  public void subscribe(String topic) {
    try {
      client.subscribe(topic, 0);
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to subscribe: " + e.getMessage());
    }
  }

  /**
   * Subscribe a topic with QoS.
   *
   * @param topic
   * @param qos
   */
  public void subscribe(String topic, int qos) {
    try {
      client.subscribe(topic, qos);
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to subscribe: " + e.getMessage());
    }
  }

  /**
   * Unsubscrbe a topic.
   *
   * @example PublishSubscribe
   * @param topic
   */
  public void unsubscribe(String topic) {
    try {
      client.unsubscribe(topic);
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to unsubscribe: " + e.getMessage());
    }
  }

  /**
   * Disconnect from the broker.
   *
   * @example PublishSubscribe
   */
  public void disconnect() {
    try {
      client.disconnect();
    } catch (MqttException e) {
      System.out.println("[MQTT] failed to disconnect!" + e.getMessage());
    }
  }

  public void dispose() {
    disconnect();
  }

  public void draw() throws Exception {
    for(Message message: messages) {
      messageReceivedMethod.invoke(parent, message.topic, message.message.getPayload());
      messages.remove(message);
    }

    messages.clear();
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    if(messageReceivedMethod != null) {
      messages.add(new Message(topic, mqttMessage));
    }
  }

  @Override
  public void connectionLost(Throwable throwable) {
    System.out.println("[MQTT] lost connection!" + throwable.getMessage());
  }

  private Method findCallback(final String name) {
    try {
      return parent.getClass().getMethod(name, String.class, byte[].class);
    } catch (Exception e) {
      System.out.println("[MQTT] messageReceived callback not found!");
      return null;
    }
  }
}
