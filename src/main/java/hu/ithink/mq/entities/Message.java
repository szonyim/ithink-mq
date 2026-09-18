package hu.ithink.mq.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
public class Message {

  @Id
  @Column(name = "message_id")
  private String messageId;

  @Lob
  @Column(name = "content", nullable = false)
  private String content;

  @Lob
  @Column(name = "properties")
  private String properties;
}
