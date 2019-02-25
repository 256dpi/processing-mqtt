package mqtt;

public interface Listener {
  void clientConnected();

  void messageReceived(String topic, byte[] payload);

  void connectionLost();
}
