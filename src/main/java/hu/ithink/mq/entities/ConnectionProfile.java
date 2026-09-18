package hu.ithink.mq.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ConnectionProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String host;

  @Column(nullable = false)
  private int port;

  @Column
  private String username;

  @Column
  private String password;

  @Column(name = "queue_manager", nullable = false)
  private String queueManager;

  @Column(nullable = false)
  private String channel;

  @Column
  private String queue;
}
