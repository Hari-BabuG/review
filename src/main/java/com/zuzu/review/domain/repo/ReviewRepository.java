package com.zuzu.review.domain.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.zuzu.review.domain.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>,JpaSpecificationExecutor<Review> {
	
	 Optional<Review> findByExternalReviewId(Long externalReviewId);
}
