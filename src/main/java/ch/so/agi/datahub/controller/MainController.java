package ch.so.agi.datahub.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnWebApplication
@RestController
public class MainController {
  @GetMapping("/ping")
  public String ping() {
    return "datahub";
  }
}
