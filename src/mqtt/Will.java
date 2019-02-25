package mqtt;

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
