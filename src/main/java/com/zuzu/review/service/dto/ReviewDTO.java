package com.zuzu.review.service.dto;

import lombok.Builder;
import lombok.Value;


import java.time.OffsetDateTime;

@Value
@Builder
public class ReviewDTO {
    Long id;
    Long externalReviewId;
    Long hotelId;
    Integer providerId;
    String languageCode;
    String commentText;
    String positives;
    String negatives;
    OffsetDateTime updatedAt;
    String rawJson; // keep as String if column is json/jsonb; map to POJO if you prefer
}
