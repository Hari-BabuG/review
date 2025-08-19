package com.zuzu.review.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.util.stream.Stream;

@Component
public class JsonlParser {
  private final ObjectMapper mapper = new ObjectMapper();

  public Stream<JsonNode> streamLines(BufferedReader reader) {
    return reader.lines().map(line -> {
      try { return mapper.readTree(line); }
      catch (Exception e) { return null; }
    }).filter(n -> n != null);
  }
}
