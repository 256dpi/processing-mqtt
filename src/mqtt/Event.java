package mqtt;

class Event {
  boolean clientConnected;
  boolean connectionLost;

  Event(boolean clientConnected, boolean connectionLost) {
    this.clientConnected = clientConnected;
    this.connectionLost = connectionLost;
  }
}
