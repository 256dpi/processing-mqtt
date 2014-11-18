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


import processing.core.*;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * An MQTTClient that can publish and subscribe.
 *
 * @example PublishSubscribe
 */

public class MQTTClient {
	PApplet myParent;

	public MqttClient client;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example PublishSubscribe
	 * @param theParent
	 */
	public MQTTClient(PApplet theParent) {
		myParent = theParent;
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	public void connect(String uri, String id) {
		this.client = new MqttClient(uri, id);
		//client.connect();
	}

	public void publish(String topic, String payload) {
	//	client.publish(topic, payload.getBytes(Charset.forName("UTF-8")););
	}

	public void subscribe(String topic) {
		//client.subscribe(topic, 0);
	}

	public void unsubscribe(String topic) {
		//client.unsubscribe(topic);
	}

	public void disconnect() {
		//client.disconnect();
	}
}
