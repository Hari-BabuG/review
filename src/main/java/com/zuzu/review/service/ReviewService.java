package com.zuzu.review.service;

import com.zuzu.review.domain.model.*;
import com.zuzu.review.domain.repo.*;
import com.zuzu.review.service.dto.ReviewDTO;
import com.zuzu.review.service.dto.ReviewFilter;
import com.zuzu.review.service.spec.ReviewSpecs;
import com.zuzu.review.ingestion.S3IngestionJob;
import com.zuzu.review.util.ValidationUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final ProviderRepository providers;
  private final HotelRepository hotels;
  private final ReviewRepository reviewRepository;

  @Transactional
  public void ingestOne(JsonNode root) {
	log.info("ingestOne Start");
    // Required fields validation
    ValidationUtils.require(root, "hotelId");
    ValidationUtils.require(root, "platform");
    ValidationUtils.require(root, "comment");

    long hotelId = root.path("hotelId").asLong();
    String hotelName = root.path("hotelName").asText(null);

    String providerName = root.path("platform").asText();
    Provider provider = providers.findByName(providerName)
        .orElseGet(() -> providers.save(new Provider(null, providerName)));

    hotels.findById(hotelId).orElseGet(() -> hotels.save(new Hotel(hotelId, hotelName)));

    JsonNode comment = root.path("comment");
    long externalId = comment.path("hotelReviewId").asLong();
    Double rating = comment.path("rating").isNumber() ? comment.path("rating").asDouble() : null;
    String title = comment.path("reviewTitle").asText(null);
    String text = comment.path("reviewComments").asText(null);
    String positives = comment.path("reviewPositives").asText(null);
    String negatives = comment.path("reviewNegatives").asText(null);
    String country = comment.path("reviewerInfo").path("countryName").asText(null);
    String lang = comment.path("translateTarget").asText(null);
    OffsetDateTime date = null;
    if (comment.hasNonNull("reviewDate")) {
      date = OffsetDateTime.parse(comment.get("reviewDate").asText());
    }

    // Upsert by (provider, external_review_id)
    Review entity = Review.builder()
        .provider(provider)
        .hotel(new Hotel(hotelId, hotelName))
        .externalReviewId(externalId)
        .rating(rating)
        .reviewDate(date)
        .title(title)
        .commentText(text)
        .positives(positives)
        .negatives(negatives)
        .reviewerCountry(country)
        .languageCode(lang)
        .rawJson(root.toString())
        .build();

    // Try insert; if unique violation then update existing
    try { reviewRepository.save(entity); }
    catch (Exception e) {
    	log.error("ingestOne Failed"+e.getMessage());
    }
    log.info("ingestOne End");
  }
  
  public Page<ReviewDTO> search(ReviewFilter filter, Pageable pageable) {
	  log.info("search Start");
      return reviewRepository.findAll(
              ReviewSpecs.build(filter),
              pageable
      ).map(this::toDto);
  }

  public ReviewDTO getById(Long id) {
	  log.info("getById Start");
      Review entity = reviewRepository.findById(id)
              .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
      return toDto(entity);
  }

  public ReviewDTO getByExternalId(Long externalId) {
	  log.info("search getByExternalId");
      Review entity = reviewRepository.findByExternalReviewId(externalId)
              .orElseThrow(() -> new IllegalArgumentException("Review not found: " + externalId));
      return toDto(entity);
  }

  private ReviewDTO toDto(Review r) {
      return ReviewDTO.builder()
              .id(r.getId())
              .externalReviewId(r.getExternalReviewId()) 
              .hotelId(r.getHotel().getId())
              .providerId(r.getProvider().getId())
              .languageCode(r.getLanguageCode())
              .commentText(r.getCommentText())
              .positives(r.getPositives())
              .negatives(r.getNegatives())
              .updatedAt(r.getUpdatedAt())
              .rawJson(r.getRawJson()) 
              .build();
  }
}

