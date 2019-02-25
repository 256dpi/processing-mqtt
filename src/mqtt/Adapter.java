package mqtt;

public class Adapter implements Listener {
  @Override
  public void clientConnected() {}

  @Override
  public void messageReceived(String topic, byte[] payload) {}

  @Override
  public void connectionLost() {}
}
