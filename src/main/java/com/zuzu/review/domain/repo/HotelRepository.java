package com.zuzu.review.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zuzu.review.domain.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {}
