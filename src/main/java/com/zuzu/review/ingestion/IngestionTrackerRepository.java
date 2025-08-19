package com.zuzu.review.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IngestionTrackerRepository extends JpaRepository<IngestionFile, String> {
    Optional<IngestionFile> findByFileName(String fileName);
}