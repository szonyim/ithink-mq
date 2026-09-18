package hu.ithink.mq.controllers;

import hu.ithink.mq.models.MqConnectionModel;
import hu.ithink.mq.services.ConnectionProfileService;
import hu.ithink.mq.services.MessageQueryService;
import hu.ithink.mq.exceptions.MqBrowseException;
import hu.ithink.mq.services.MqQueueBrowseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class IndexController {

  private final MessageQueryService messageQueryService;
  private final MqQueueBrowseService mqQueueBrowseService;
  private final ConnectionProfileService connectionProfileService;

  IndexController(MessageQueryService messageQueryService, MqQueueBrowseService mqQueueBrowseService,
                   ConnectionProfileService connectionProfileService) {
    this.messageQueryService = messageQueryService;
    this.mqQueueBrowseService = mqQueueBrowseService;
    this.connectionProfileService = connectionProfileService;
  }

  @GetMapping("/")
  public String index(@RequestParam(required = false) String query,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
    model.addAttribute("mqConnectionModel", new MqConnectionModel());
    model.addAttribute("messagePage", messageQueryService.search(query, page));
    model.addAttribute("connectionProfiles", connectionProfileService.findAll());
    return "message/index";
  }

  @PostMapping("/load")
  public String load(@ModelAttribute MqConnectionModel mqConnectionModel, Model model) {
    model.addAttribute("mqConnectionModel", mqConnectionModel);
    try {
      int loaded = mqQueueBrowseService.browseAndStore(mqConnectionModel);
      model.addAttribute("loadSuccess", loaded + " message(s) loaded from queue '" + mqConnectionModel.queue() + "'.");
    } catch (MqBrowseException e) {
      model.addAttribute("loadError", e.getMessage());
    }
    model.addAttribute("messagePage", messageQueryService.search(null, 0));
    model.addAttribute("connectionProfiles", connectionProfileService.findAll());
    return "message/index";
  }
}
