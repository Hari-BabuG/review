package com.zuzu.review.service.spec;

import com.zuzu.review.domain.model.Review;
import com.zuzu.review.service.dto.ReviewFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ReviewSpecs {

    private ReviewSpecs() {}

    public static Specification<Review> build(ReviewFilter reviewFilter) {
        return (root, cq, cb) -> {
            List<jakarta.persistence.criteria.Predicate> p = new ArrayList<>();

            if (reviewFilter.getHotelId() != null) {
                p.add(cb.equal(root.get("hotel").get("id"), reviewFilter.getHotelId()));
            }
            if (reviewFilter.getProviderId() != null) {
                p.add(cb.equal(root.get("provider").get("id"), reviewFilter.getProviderId()));
            }
            if (reviewFilter.getLanguageCode() != null && !reviewFilter.getLanguageCode().isBlank()) {
                p.add(cb.equal(cb.lower(root.get("languageCode")), reviewFilter.getLanguageCode().toLowerCase()));
            }
            if (reviewFilter.getFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), reviewFilter.getFrom()));
            }
            if (reviewFilter.getTo() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("updatedAt"), reviewFilter.getTo()));
            }
            if (reviewFilter.getQ() != null && !reviewFilter.getQ().isBlank()) {
                String like = "%" + reviewFilter.getQ().toLowerCase() + "%";
                var commentLike = cb.like(cb.lower(root.get("commentText")), like);
                // rawJson is mapped as String (json/jsonb); safe to LIKE in most dialects.
                var jsonLike = cb.like(cb.lower(root.get("rawJson")), like);
                p.add(cb.or(commentLike, jsonLike));
            }

            return cb.and(p.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}   

