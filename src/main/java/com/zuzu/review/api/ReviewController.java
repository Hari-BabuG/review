package com.zuzu.review.api;

import com.zuzu.review.service.ReviewService;
import com.zuzu.review.service.dto.ReviewDTO;
import com.zuzu.review.service.dto.ReviewFilter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	// GET
	// /api/reviews?hotelId=...&providerId=...&language=...&from=...&to=...&q=...
	@GetMapping
	public Page<ReviewDTO> list(@RequestParam(required = false) Long hotelId,
			@RequestParam(required = false) Long providerId, @RequestParam(required = false) String language,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(required = false) String q, // free-text search in comment/rawJson
			Pageable pageable) {
		log.info("list Start");
		ReviewFilter filter = ReviewFilter.builder().hotelId(hotelId).providerId(providerId).languageCode(language)
				.from(from).to(to).q(q).build();

		return reviewService.search(filter, pageable);
	}

	// GET /api/reviews/{id}
	@GetMapping("/{id}")
	public ReviewDTO getById(@PathVariable Long id) {
		log.info("getById Start");
		return reviewService.getById(id);
	}

	// Optional: GET /api/reviews/external/{externalId}
	@GetMapping("/external/{externalId}")
	public ReviewDTO getByExternalId(@PathVariable Long externalId) {
		log.info("getByExternalId Start");
		return reviewService.getByExternalId(externalId);
	}
}
