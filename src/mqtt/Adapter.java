package mqtt;

/**
 * Adapter is an abstract class that can be used to implement a listener.
 */
abstract public class Adapter implements Listener {
  @Override
  public void clientConnected() {}

  @Override
  public void messageReceived(String topic, byte[] payload) {}

  @Override
  public void connectionLost() {}
}
