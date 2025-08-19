package com.zuzu.review.util;

import com.fasterxml.jackson.databind.JsonNode;

public class ValidationUtils {
  public static void require(JsonNode node, String field) {
    if (!node.hasNonNull(field)) {
      throw new IllegalArgumentException("Missing required field: " + field);
    }
  }
}
