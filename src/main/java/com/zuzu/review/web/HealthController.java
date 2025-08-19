package com.zuzu.review.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("/ping")
  public Map<String,Object> ping(){
    return Map.of("ok", true);
  }
}
