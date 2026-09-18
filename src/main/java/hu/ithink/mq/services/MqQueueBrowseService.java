package hu.ithink.mq.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hu.ithink.mq.exceptions.MqBrowseException;
import tools.jackson.databind.ObjectMapper;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;

import hu.ithink.mq.entities.Message;
import hu.ithink.mq.models.MqConnectionModel;
import org.springframework.stereotype.Service;

// Non-destructively browses every message currently on the queue (they stay on MQ)
// and upserts them into the local message table for the searchable UI.
@Service
public class MqQueueBrowseService {

  private static final HexFormat HEX = HexFormat.of().withUpperCase();

  private final MessageService messageService;
  private final ObjectMapper objectMapper;

  public MqQueueBrowseService(MessageService messageService, ObjectMapper objectMapper) {
    this.messageService = messageService;
    this.objectMapper = objectMapper;
  }

  public int browseAndStore(MqConnectionModel connection) {
    MQQueueManager queueManager = connect(connection);
    try {
      MQQueue queue = openQueueForBrowsing(queueManager, connection.queue());
      try {
        List<Message> messages = browseAll(queue);
        messageService.saveAll(messages);
        return messages.size();
      } finally {
        closeQuietly(queue);
      }
    } finally {
      disconnectQuietly(queueManager);
    }
  }

  private MQQueueManager connect(MqConnectionModel connection) {
    Hashtable<String, Object> properties = new Hashtable<>();
    properties.put(CMQC.HOST_NAME_PROPERTY, connection.host());
    properties.put(CMQC.CHANNEL_PROPERTY, connection.channel());
    properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
    if (connection.user() != null && !connection.user().isBlank()) {
      properties.put(CMQC.USER_ID_PROPERTY, connection.user());
      properties.put(CMQC.PASSWORD_PROPERTY, connection.password());
    }
    try {
      properties.put(CMQC.PORT_PROPERTY, Integer.parseInt(connection.port()));
    } catch (NumberFormatException e) {
      throw new MqBrowseException("Érvénytelen port: " + connection.port(), e);
    }

    try {
      return new MQQueueManager(connection.queueManager(), properties);
    } catch (MQException e) {
      throw new MqBrowseException("Nem sikerült kapcsolódni az MQ szerverhez: " + e.getMessage(), e);
    }
  }

  private MQQueue openQueueForBrowsing(MQQueueManager queueManager, String queueName) {
    try {
      int openOptions = CMQC.MQOO_BROWSE | CMQC.MQOO_INPUT_SHARED | CMQC.MQOO_FAIL_IF_QUIESCING;
      return queueManager.accessQueue(queueName, openOptions);
    } catch (MQException e) {
      throw new MqBrowseException("Nem sikerült megnyitni a(z) '" + queueName + "' queue-t: " + e.getMessage(), e);
    }
  }

  private List<Message> browseAll(MQQueue queue) {
    List<Message> messages = new ArrayList<>();
    MQGetMessageOptions options = new MQGetMessageOptions();
    options.options = CMQC.MQGMO_BROWSE_NEXT | CMQC.MQGMO_NO_WAIT | CMQC.MQGMO_CONVERT
        | CMQC.MQGMO_FAIL_IF_QUIESCING;

    while (true) {
      MQMessage mqMessage = new MQMessage();
      try {
        queue.get(mqMessage, options);
      } catch (MQException e) {
        if (e.getReason() == CMQC.MQRC_NO_MSG_AVAILABLE) {
          break;
        }
        throw new MqBrowseException("Hiba az üzenetek olvasása közben: " + e.getMessage(), e);
      }
      messages.add(toEntity(mqMessage));
    }
    return messages;
  }

  private Message toEntity(MQMessage mqMessage) {
    Message message = new Message();
    message.setMessageId(HEX.formatHex(mqMessage.messageId));
    message.setContent(readContent(mqMessage));
    message.setProperties(writeProperties(mqMessage));
    return message;
  }

  private String readContent(MQMessage mqMessage) {
    try {
      return mqMessage.readStringOfByteLength(mqMessage.getDataLength());
    } catch (Exception e) {
      throw new MqBrowseException("Nem sikerült beolvasni egy üzenet tartalmát: " + e.getMessage(), e);
    }
  }

  private String writeProperties(MQMessage mqMessage) {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("format", mqMessage.format == null ? null : mqMessage.format.trim());
    properties.put("priority", mqMessage.priority);
    properties.put("persistence", mqMessage.persistence);
    properties.put("characterSet", mqMessage.characterSet);
    properties.put("encoding", mqMessage.encoding);
    properties.put("expiry", mqMessage.expiry);
    properties.put("report", mqMessage.report);
    properties.put("messageType", mqMessage.messageType);
    properties.put("feedback", mqMessage.feedback);
    properties.put("backoutCount", mqMessage.backoutCount);
    properties.put("putApplicationName", mqMessage.putApplicationName);
    properties.put("putApplicationType", mqMessage.putApplicationType);
    properties.put("putDateTime", mqMessage.putDateTime == null ? null : mqMessage.putDateTime.toInstant().toString());
    properties.put("userId", mqMessage.userId);
    properties.put("replyToQueueName", mqMessage.replyToQueueName);
    properties.put("replyToQueueManagerName", mqMessage.replyToQueueManagerName);
    properties.put("correlationId", HEX.formatHex(mqMessage.correlationId));
    properties.put("groupId", HEX.formatHex(mqMessage.groupId));
    properties.put("originalLength", mqMessage.originalLength);

    try {
      return objectMapper.writeValueAsString(properties);
    } catch (Exception e) {
      throw new MqBrowseException("Nem sikerült JSON-ná alakítani egy üzenet tulajdonságait: " + e.getMessage(), e);
    }
  }

  private void closeQuietly(MQQueue queue) {
    try {
      queue.close();
    } catch (MQException ignored) {
      // best-effort cleanup
    }
  }

  private void disconnectQuietly(MQQueueManager queueManager) {
    try {
      queueManager.disconnect();
    } catch (MQException ignored) {
      // best-effort cleanup
    }
  }
}
