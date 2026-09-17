package models;

import lombok.Data;

@Data
public class MqConnectionModel {
  private String host;
  private String port;
  private String user;
  private String password;
  private String queueManager;
  private String channel;
  private String queue;
}
