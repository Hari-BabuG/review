package com.zuzu.review.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zuzu.review.domain.model.Provider;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Integer> {
	  Optional<Provider> findByName(String name);
}