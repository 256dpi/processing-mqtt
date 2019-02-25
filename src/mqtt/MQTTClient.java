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
import java.lang.Throwable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;
import processing.core.*;

/** An MQTTClient that can publish and subscribe. */
public class MQTTClient implements MqttCallbackExtended {
  private MqttAsyncClient client;
  private PApplet parent;
  private CopyOnWriteArrayList<Message> messages;
  private CopyOnWriteArrayList<Event> events;
  private Will will;
  private MQTTListener listener;
  private Method messageReceivedMethod;
  private Method clientConnectedMethod;
  private Method connectionLostMethod;

  private final static long TIMEOUT = 2000;

  /**
   * The constructor, usually called in the setup() method in your sketch to initialize and start
   * the library.
   *
   * @param parent A reference to the running sketch.
   */
  public MQTTClient(PApplet parent) {
    // save parent
    this.parent = parent;

    // init messages and events list
    messages = new CopyOnWriteArrayList<>();
    events = new CopyOnWriteArrayList<>();

    // register callbacks
    parent.registerMethod("dispose", this);
    parent.registerMethod("draw", this);

    // find callbacks
    messageReceivedMethod = findMessageReceivedCallback();
    clientConnectedMethod = findClientConnectedCallback();
    connectionLostMethod = findConnectionLostCallback();
  }

  /**
   * The constructor, usually called in the setup() method in your sketch to initialize and start
   * the library.
   *
   * @param parent A reference to the running sketch.
   * @param listener A class that receives events.
   */
  public MQTTClient(PApplet parent, MQTTListener listener) {
    // save parent
    this.parent = parent;
    this.listener = listener;

    // init messages and events list
    messages = new CopyOnWriteArrayList<>();
    events = new CopyOnWriteArrayList<>();

    // register callbacks
    parent.registerMethod("dispose", this);
    parent.registerMethod("draw", this);
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
  public void connect(String brokerURI) {
    connect(brokerURI, MqttClient.generateClientId());
  }

  /**
   * Connect to a broker using an broker URI and client ID.
   *
   * @param brokerURI The broker URI.
   * @param clientID The client ID.
   */
  public void connect(String brokerURI, String clientID) {
    connect(brokerURI, clientID, true);
  }

  /**
   * Connect to a broker using an broker URI, client Id and cleanSession flag.
   *
   * @param brokerURI The broker URI.
   * @param clientId The client ID.
   * @param cleanSession The clean session flag.
   */
  public void connect(String brokerURI, String clientId, boolean cleanSession) {
    // parse uri
    URI uri;
    try {
      uri = new URI(brokerURI);
    } catch (URISyntaxException e) {
      throw new RuntimeException("[MQTT] Failed to parse URI: " + e.getMessage(), e);
    }

    try {
      // prepare connection options
      MqttConnectOptions options = new MqttConnectOptions();
      options.setCleanSession(cleanSession);
      options.setAutomaticReconnect(true);

      // check will messafe
      if (will != null) {
        options.setWill(will.topic, will.payload, will.qos, will.retained);
      }

      // check for auth info
      if (uri.getUserInfo() != null) {
        String[] auth = uri.getUserInfo().split(":");

        // check username
        if (auth.length > 0) {
          options.setUserName(auth[0]);

          // check passsword
          if (auth.length > 1) {
            options.setPassword(auth[1].toCharArray());
          }
        }
      }

      // parse scheme
      String scheme = uri.getScheme();
      if (scheme.equals("mqtt")) {
        scheme = "tcp";
      } else if (scheme.equals("mqtts")) {
        scheme = "ssl";
      }

      // finalize location
      String loc = scheme + "://" + uri.getHost();
      if (uri.getPort() != -1) {
        loc = loc + ":" + uri.getPort();
      }

      // create client
      client = new MqttAsyncClient(loc, clientId, new MemoryPersistence());
      client.setCallback(this);

      // connect to broker
      client.connect(options).waitForCompletion(TIMEOUT);
    } catch (MqttException e) {
      throw new RuntimeException("[MQTT] Failed to connect:: " + e.getMessage(), e);
    }
  }

  /**
   * Publish a message with a topic.
   *
   * @param topic The topic.
   */
  public void publish(String topic) {
    byte[] bytes = {};
    publish(topic, bytes);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @param topic The topic.
   * @param payload The payload.
   */
  public void publish(String topic, String payload) {
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
  public void publish(String topic, String payload, int qos, boolean retained) {
    publish(topic, payload.getBytes(Charset.forName("UTF-8")), qos, retained);
  }

  /**
   * Publish a message with a topic and payload.
   *
   * @param topic The topic.
   * @param payload The payload.
   */
  public void publish(String topic, byte[] payload) {
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
  public void publish(String topic, byte[] payload, int qos, boolean retained) {
    try {
      client.publish(topic, payload, qos, retained).waitForCompletion(TIMEOUT);
    } catch (MqttException e) {
      throw new RuntimeException("[MQTT] Failed to publish: " + e.getMessage(), e);
    }
  }

  /**
   * Subscribe a topic.
   *
   * @param topic The topic.
   */
  public void subscribe(String topic) {
    this.subscribe(topic, 0);
  }

  /**
   * Subscribe a topic with QoS.
   *
   * @param topic The topic.
   * @param qos The qos level.
   */
  public void subscribe(String topic, int qos) {
    try {
      client.subscribe(topic, qos).waitForCompletion(TIMEOUT);
    } catch (MqttException e) {
      throw new RuntimeException("[MQTT] Failed to subscribe: " + e.getMessage(), e);
    }
  }

  /**
   * Unsubscribe a topic.
   *
   * @param topic The topic.
   */
  public void unsubscribe(String topic) {
    try {
      client.unsubscribe(topic).waitForCompletion(TIMEOUT);
    } catch (MqttException e) {
      throw new RuntimeException("[MQTT] Failed to unsubscribe: " + e.getMessage(), e);
    }
  }

  /** Disconnect from the broker. */
  public void disconnect() {
    try {
      client.disconnect().waitForCompletion(TIMEOUT);
    } catch (MqttException e) {
      throw new RuntimeException("[MQTT] Failed to disconnect: " + e.getMessage(), e);
    }
  }

  public void dispose() {
    disconnect();
  }

  public void draw() {
    // process all queue events
    for (Event event : events) {
      // invoke client connected callback
      if (event.clientConnected) {
        if(listener != null) {
          try {
            listener.clientConnected();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else if (clientConnectedMethod != null) {
          try {
            clientConnectedMethod.invoke(parent);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }

      // invoke connection lost callback
      if (event.connectionLost) {
        if(listener != null) {
          try {
            listener.connectionLost();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else if (connectionLostMethod != null) {
          try {
            connectionLostMethod.invoke(parent);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    // process all messages and invoke message received callback
    for (Message message : messages) {
      if(listener != null) {
        try {
          listener.messageReceived(message.topic, message.message.getPayload());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else if (messageReceivedMethod != null) {
        try {
          messageReceivedMethod.invoke(parent, message.topic, message.message.getPayload());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    // clear all queued events and messages
    events.clear();
    messages.clear();
  }

  @Override
  public void connectComplete(boolean b, String s) {
    events.add(new Event(true, false));
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    messages.add(new Message(topic, mqttMessage));
  }

  @Override
  public void connectionLost(Throwable throwable) {
    events.add(new Event(false, true));
  }

  private Method findMessageReceivedCallback() {
    try {
      return parent.getClass().getMethod("messageReceived", String.class, byte[].class);
    } catch (Exception e) {
      throw new RuntimeException("MQTT] Callback not found!", e);
    }
  }

  private Method findClientConnectedCallback() {
    try {
      return parent.getClass().getMethod("clientConnected");
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
