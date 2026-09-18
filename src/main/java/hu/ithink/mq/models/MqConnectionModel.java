package hu.ithink.mq.models;

public record MqConnectionModel(String host, String port, String user, String password, String queueManager,
                                 String channel, String queue) {

  public MqConnectionModel() {
    this("localhost", "1414", null, null, "QM1", "SYSTEM.ADMIN.SVRCONN", null);
  }
}
