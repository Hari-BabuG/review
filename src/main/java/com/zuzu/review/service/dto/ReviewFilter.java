package com.zuzu.review.service.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ReviewFilter {
    Long hotelId;
    Long providerId;
    String languageCode;
    Instant from; // updatedAt >= from
    Instant to;   // updatedAt <= to
    String q;     // free-text search (commentText/rawJson)
}
