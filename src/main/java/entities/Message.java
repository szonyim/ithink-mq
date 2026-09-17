package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Message {

  @Id
  private String messageId;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "properties")
  private String properties;
}
