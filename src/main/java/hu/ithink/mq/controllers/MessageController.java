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

@Controller
class MessageController {

  private final MessageService messageService;
  private final MessageQueryService messageQueryService;

  MessageController(MessageService messageService, MessageQueryService messageQueryService) {
    this.messageService = messageService;
    this.messageQueryService = messageQueryService;
  }

  @GetMapping("/messages/search")
  public String search(@RequestParam(required = false) String query,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
    model.addAttribute("messagePage", messageQueryService.search(query, page));
    return "message/messages-table :: table";
  }

  @GetMapping("/messages/{messageId}")
  public String view(@PathVariable String messageId, Model model) {
    Message message = messageService.findById(messageId);
    if (message == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found: " + messageId);
    }
    model.addAttribute("message", message);
    return "message/message-view";
  }

  @PostMapping("/messages/{messageId}/delete")
  public String delete(@PathVariable String messageId,
                        @RequestParam(required = false) String query,
                        @RequestParam(defaultValue = "0") int page) {
    messageService.deleteById(messageId);
    return "redirect:/?query=" + encode(query) + "&page=" + page;
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
