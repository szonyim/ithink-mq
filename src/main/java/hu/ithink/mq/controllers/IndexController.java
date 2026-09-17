package hu.ithink.mq.controllers;

import models.MqConnectionModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class IndexController {

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("mqConnectionModel", new MqConnectionModel());
    return "index";
  }

  @PostMapping("/load")
  public String load(@ModelAttribute MqConnectionModel mqConnectionModel, Model model) {
    model.addAttribute("mqConnectionModel", mqConnectionModel);
    return "index";
  }



}
