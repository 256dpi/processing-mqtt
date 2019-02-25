package mqtt;

/**
 * MQTTListener is an interface that can be implemented to receive MQTT events.
 */
public interface MQTTListener {
  /**
   * This method is called once the client successfully connected.
   */
  void clientConnected();

  /**
   * This method is called once the client received a message.
   * @param topic The topic.
   * @param payload The payload.
   */
  void messageReceived(String topic, byte[] payload);

  /**
   * This method is called once the client has lost connection.
   */
  void connectionLost();
}
