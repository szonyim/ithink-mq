package hu.ithink.mq.services;

import java.util.List;

import hu.ithink.mq.entities.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import hu.ithink.mq.repositories.MessageRepository;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final MessageRepository messageRepository;

  public Message findById(String messageId) {
    return messageRepository.findById(messageId).orElse(null);
  }

  public void saveAll(List<Message> messages) {
    messageRepository.saveAll(messages);
  }

  public void deleteById(String messageId) {
    messageRepository.deleteById(messageId);
  }

  public void purgeAll() {
    messageRepository.deleteAllInBatch();
  }
}
