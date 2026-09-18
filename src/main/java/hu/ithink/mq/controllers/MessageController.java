package hu.ithink.mq.controllers;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

import hu.ithink.mq.entities.Message;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import hu.ithink.mq.services.MessageQueryService;
import hu.ithink.mq.services.MessageService;
import tools.jackson.databind.ObjectMapper;

@Controller
class MessageController {

  private final MessageService messageService;
  private final MessageQueryService messageQueryService;
  private final ObjectMapper objectMapper;

  MessageController(MessageService messageService, MessageQueryService messageQueryService, ObjectMapper objectMapper) {
    this.messageService = messageService;
    this.messageQueryService = messageQueryService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/messages/search")
  public String search(@RequestParam(required = false) String query,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "" + MessageQueryService.DEFAULT_PAGE_SIZE) int pageSize,
                          Model model) {
    model.addAttribute("messagePage", messageQueryService.search(query, page, pageSize));
    return "message/messages-table :: table";
  }

  @GetMapping("/messages/{messageId}")
  public String view(@PathVariable String messageId, Model model) {
    Message message = messageService.findById(messageId);
    if (message == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found: " + messageId);
    }
    message.setContent(prettyPrintIfJson(message.getContent()));
    message.setProperties(prettyPrintIfJson(message.getProperties()));
    model.addAttribute("message", message);
    return "message/message-view";
  }

  private String prettyPrintIfJson(String text) {
    if (text == null) {
      return null;
    }
    try {
      Object json = objectMapper.readValue(text, Object.class);
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (Exception e) {
      return text;
    }
  }

  @PostMapping("/messages/{messageId}/delete")
  public String delete(@PathVariable String messageId,
                        @RequestParam(required = false) String query,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "" + MessageQueryService.DEFAULT_PAGE_SIZE) int pageSize) {
    messageService.deleteById(messageId);
    return "redirect:/?query=" + encode(query) + "&page=" + page + "&pageSize=" + pageSize;
  }

  @PostMapping("/messages/purge")
  public String purge() {
    messageService.purgeAll();
    return "redirect:/";
  }

  private String encode(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }
}
