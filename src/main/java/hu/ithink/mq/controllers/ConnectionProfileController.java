package hu.ithink.mq.controllers;

import hu.ithink.mq.entities.ConnectionProfile;
import hu.ithink.mq.services.ConnectionProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/connection-profile")
class ConnectionProfileController {

  private final ConnectionProfileService connectionProfileService;

  ConnectionProfileController(ConnectionProfileService connectionProfileService) {
    this.connectionProfileService = connectionProfileService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("connectionProfiles", connectionProfileService.findAll());
    return "connection-profile/list";
  }

  @GetMapping("/add")
  public String add(Model model) {
    model.addAttribute("connectionProfile", new ConnectionProfile());
    return "connection-profile/add";
  }

  @PostMapping("/add")
  public String create(@ModelAttribute ConnectionProfile connectionProfile) {
    connectionProfileService.save(connectionProfile);
    return "redirect:/connection-profile";
  }

  @GetMapping("/{id}/edit")
  public String edit(@PathVariable Long id, Model model) {
    ConnectionProfile connectionProfile = connectionProfileService.findById(id);
    if (connectionProfile == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found: " + id);
    }
    model.addAttribute("connectionProfile", connectionProfile);
    return "connection-profile/edit";
  }

  @PostMapping("/{id}/edit")
  public String update(@PathVariable Long id, @ModelAttribute ConnectionProfile connectionProfile) {
    connectionProfile.setId(id);
    connectionProfileService.save(connectionProfile);
    return "redirect:/connection-profile";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    connectionProfileService.deleteById(id);
    return "redirect:/connection-profile";
  }
}
