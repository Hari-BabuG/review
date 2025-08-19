package com.zuzu.review.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "reviews",
  uniqueConstraints = @UniqueConstraint(name = "ux_review_provider_external",
    columnNames = {"provider_id","external_review_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false) @JoinColumn(name = "provider_id")
  private Provider provider;

  @ManyToOne(optional = false) @JoinColumn(name = "hotel_id")
  private Hotel hotel;

  @Column(name = "external_review_id", nullable = false)
  private Long externalReviewId;

  private Double rating;

  @Column(name = "review_date")
  private OffsetDateTime reviewDate;

  private String title;

  @Column(name = "comment_text")
  private String commentText;

  private String positives;
  private String negatives;

  @Column(name = "reviewer_country")
  private String reviewerCountry;

  @Column(name = "language_code")
  private String languageCode;

  @JdbcTypeCode(SqlTypes.JSON)                
  @Column(name = "raw_json", columnDefinition = "jsonb")
  private String rawJson; // full raw JSON for future-proofing

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
