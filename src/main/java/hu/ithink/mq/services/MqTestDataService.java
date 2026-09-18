package hu.ithink.mq.services;

import java.time.Instant;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import hu.ithink.mq.exceptions.MqPutException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

// Puts a batch of generated sample messages onto the developer queue, so the browse/search
// flow can be exercised without a real upstream producer. Uses the connection details
// documented in CLAUDE.md under "Developer queue connection data".
@Service
public class MqTestDataService {

  private static final int MESSAGE_COUNT = 10;

  private static final String HOST = "localhost";
  private static final int PORT = 1414;
  private static final String QUEUE_MANAGER = "QM1";
  private static final String CHANNEL = "SYSTEM.ADMIN.SVRCONN";
  private static final String QUEUE = "MAIN.QUEUE";

  private static final List<String> CUSTOMERS = List.of(
      "Alice Johnson", "Bob Smith", "Carol Williams", "David Brown", "Emma Davis");
  private static final List<String> PRODUCTS = List.of(
      "Keyboard", "Monitor", "Mouse", "Headset", "Webcam");

  private final ObjectMapper objectMapper;

  public MqTestDataService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public int loadTestData() {
    MQQueueManager queueManager = connect();
    try {
      MQQueue queue = openQueueForOutput(queueManager);
      try {
        for (int i = 0; i < MESSAGE_COUNT; i++) {
          put(queue, generatePayload());
        }
        return MESSAGE_COUNT;
      } finally {
        closeQuietly(queue);
      }
    } finally {
      disconnectQuietly(queueManager);
    }
  }

  private MQQueueManager connect() {
    Hashtable<String, Object> properties = new Hashtable<>();
    properties.put(CMQC.HOST_NAME_PROPERTY, HOST);
    properties.put(CMQC.PORT_PROPERTY, PORT);
    properties.put(CMQC.CHANNEL_PROPERTY, CHANNEL);
    properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
    try {
      return new MQQueueManager(QUEUE_MANAGER, properties);
    } catch (MQException e) {
      throw new MqPutException("Failed to connect to the MQ server: " + e.getMessage(), e);
    }
  }

  private MQQueue openQueueForOutput(MQQueueManager queueManager) {
    try {
      int openOptions = CMQC.MQOO_OUTPUT | CMQC.MQOO_FAIL_IF_QUIESCING;
      return queueManager.accessQueue(QUEUE, openOptions);
    } catch (MQException e) {
      throw new MqPutException("Failed to open queue '" + QUEUE + "': " + e.getMessage(), e);
    }
  }

  private void put(MQQueue queue, String content) {
    MQMessage message = new MQMessage();
    message.format = CMQC.MQFMT_STRING;
    message.persistence = CMQC.MQPER_PERSISTENT;
    try {
      message.writeString(content);
      queue.put(message, new MQPutMessageOptions());
    } catch (Exception e) {
      throw new MqPutException("Failed to put message onto the queue: " + e.getMessage(), e);
    }
  }

  private String generatePayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("orderId", UUID.randomUUID().toString());
    payload.put("customer", randomOf(CUSTOMERS));
    payload.put("product", randomOf(PRODUCTS));
    payload.put("quantity", ThreadLocalRandom.current().nextInt(1, 10));
    payload.put("amount", Math.round(ThreadLocalRandom.current().nextDouble(1_000, 100_000) * 100.0) / 100.0);
    payload.put("createdAt", Instant.now().toString());
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception e) {
      throw new MqPutException("Failed to serialize the test message to JSON: " + e.getMessage(), e);
    }
  }

  private String randomOf(List<String> values) {
    return values.get(ThreadLocalRandom.current().nextInt(values.size()));
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
