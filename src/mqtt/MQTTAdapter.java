package mqtt;

/**
 * MQTTAdapter is an abstract class that can be used to implement a listener.
 */
abstract public class MQTTAdapter implements MQTTListener {
  @Override
  public void clientConnected() {}

  @Override
  public void messageReceived(String topic, byte[] payload) {}

  @Override
  public void connectionLost() {}
}
