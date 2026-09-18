package hu.ithink.mq.controllers;

import hu.ithink.mq.exceptions.MqPutException;
import hu.ithink.mq.services.MqTestDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class TestDataController {

  private final MqTestDataService mqTestDataService;

  TestDataController(MqTestDataService mqTestDataService) {
    this.mqTestDataService = mqTestDataService;
  }

  @PostMapping("/test-data/load")
  @ResponseBody
  public ResponseEntity<String> load() {
    try {
      int loaded = mqTestDataService.loadTestData();
      return ResponseEntity.ok(loaded + " test message(s) sent to the MQ queue.");
    } catch (MqPutException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
