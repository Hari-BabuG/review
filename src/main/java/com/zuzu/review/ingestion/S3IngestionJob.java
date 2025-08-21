package com.zuzu.review.ingestion;

import com.zuzu.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class S3IngestionJob {
	private final S3Service s3Service;
	private final JsonlParser parser;
	private final ReviewService reviewService;
	private final IngestionTrackerRepository tracker;
	@Qualifier("ingestionExecutor")
	private final Executor ingestionExecutor;

	@Value("${app.ingestion.parallelism:4}")
	private int parallelism;
	
	private StringRedisTemplate redisTemplate;

	public S3IngestionJob(S3Service s3Service, JsonlParser parser, ReviewService reviewService,
			IngestionTrackerRepository tracker, @Qualifier("ingestionExecutor") Executor ingestionExecutor, @Nullable StringRedisTemplate redisTemplate) {
		this.s3Service = s3Service;
		this.parser = parser;
		this.reviewService = reviewService;
		this.tracker = tracker;
		this.ingestionExecutor = ingestionExecutor;
		this.redisTemplate = redisTemplate;
		
	}

	// Cron-compatible: run via fixedRate or external scheduler calling an endpoint/command
	@Scheduled(fixedDelayString = "${app.ingestion.schedule:PT5M}")
	public void scheduledPoll() {
		log.info("scheduledPoll Start");
		this.pollOnce();
		log.info("scheduledPoll End");
	}

	public void pollOnce() {
		log.info("pollOnce Start");
		List<S3Object> objects = s3Service.listNewObjects();
		log.info("pollOnce result size" + objects.size());
		objects.stream().limit(1000).forEach(obj -> submitIfNew(obj));
		log.info("pollOnce End");
	}
	
	private void submitIfNew(S3Object obj) {
	    log.info("submitIfNew id: {}", obj.key());
	    String fileId = obj.key();

	    // Fast path: cache hit (only if Redis is available)
	    if (redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(fileId))) {
	        log.info("Cache hit for {}, skipping ingestion", fileId);
	        return;
	    }

	    log.info("Will hit DB for {}", fileId);

	    // Cache miss: check DB
	    tracker.findById(fileId).ifPresentOrElse(
	        existing -> {
	            log.info("Cache miss + DB hit for {}, skipping", fileId);
	            if (redisTemplate != null) {
	                redisTemplate.opsForValue().set(fileId, "SKIPPED");
	            }
	        },
	        () -> ingestionExecutor.execute(() -> processObject(obj)) // proceed with ingestion
	    );
	}
	
	

	@Async("ingestionExecutor")
	@Transactional
	public void processObject(S3Object obj) {
		IngestionFile ingestionFile = IngestionFile.builder().id(obj.key()).fileName(obj.key()).status("PENDING")
				.build();
		tracker.save(ingestionFile);
		try (BufferedReader reader = s3Service.openObject(obj)) {
			parser.streamLines(reader).forEach(node -> {
				try {
					reviewService.ingestOne(node);
				} catch (Exception e) {
					log.warn("Bad record in {}: {}", obj.key(), e.getMessage());
				}
			});
			ingestionFile.setStatus("SUCCESS");
			ingestionFile.setProcessedAt(Instant.now());
			tracker.save(ingestionFile);
			log.info("Ingested {}", obj.key());
		} catch (Exception e) {
			ingestionFile.setStatus("FAILED");
			ingestionFile.setProcessedAt(Instant.now());
			ingestionFile.setError(e.getMessage());
			tracker.save(ingestionFile);
			log.error("Failed to ingest {}: {}", obj.key(), e.getMessage());
		}
	}
}