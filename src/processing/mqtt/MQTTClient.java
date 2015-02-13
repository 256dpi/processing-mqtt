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

package processing.mqtt;

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
import java.util.ArrayList;

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

  Message(String _topic, MqttMessage _message) {
    topic = _topic;
    message = _message;
  }
}


/**
 * An MQTTClient that can publish and subscribe.
 *
 * @example PublishSubscribe
 */

public class MQTTClient implements MqttCallback {
	PApplet parent;

  ArrayList<Message> messages;

	Method messageReceivedMethod;

	public MqttClient client;

	/**
	 * The constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 *
	 * @example PublishSubscribe
	 * @param theParent
	 */
	public MQTTClient(PApplet theParent) {
		parent = theParent;
    messages = new ArrayList<Message>(10);
		parent.registerMethod("dispose", this);
    parent.registerMethod("draw", this);
		messageReceivedMethod = findCallback("messageReceived");
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * Connect to a broker using a URI and clientId.
	 *
	 * @example PublishSubscribe
	 * @param theUri
	 * @param theId
	 */
	public void connect(String theUri, String theID) {
		URI uri = null;
		try {
			uri = new URI(theUri);
		} catch(URISyntaxException e) {
			System.out.println("[MQTT] failed to parse URI: " + e.getMessage());
		}

	 	try {
			MqttConnectOptions options = new MqttConnectOptions();

			String[] auth = uri.getUserInfo().split(":");
			if(auth.length > 0) {
				String user = auth[0];
				String pass = auth[1];
				options.setUserName(user);
				options.setPassword(pass.toCharArray());
			}

			if (uri.getPort()!=-1){
				client = new MqttClient("tcp://" + uri.getHost() + ":" + uri.getPort(), theID, new MemoryPersistence());
			} else {
				client = new MqttClient("tcp://" + uri.getHost(), theID, new MemoryPersistence());
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
		this.publish(topic, bytes);
	}

	/**
	 * Publish a message with a topic and payload.
	 *
	 * @example PublishSubscribe
	 * @param topic
	 * @param payload
	 */
	public void publish(String topic, String payload) {
		this.publish(topic, payload.getBytes(Charset.forName("UTF-8")));
	}

	/**
	 * Publish a message with a topic and payload.
	 *
	 * @param topic
	 * @param payload
	 */
	public void publish(String topic, byte[] payload) {
		this.publish(topic, payload, 0, false);
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
    synchronized (messages) {
      for(Message message: messages) {
        messageReceivedMethod.invoke(parent, message.topic, message.message.getPayload());
      }
      messages.clear();
    }
  }

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) {
    if(messageReceivedMethod != null) {
      synchronized (messages) {
        messages.add(new Message(topic, mqttMessage));
      }
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
