package com.zuzu.review.test;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import com.zuzu.review.domain.repo.ReviewRepository;
import com.zuzu.review.ingestion.S3IngestionJob;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers // enables Testcontainers lifecycle
@SpringBootTest(properties = { "spring.task.scheduling.enabled=false", "app.ingestion.schedule.enabled=false" }) // boots your Spring context for the test
@ActiveProfiles("test") // uses application-test.yml (H2, etc.)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewPipelineIT {

	private static final String BUCKET = "my-test-bucket";

	@Container
	static final LocalStackContainer localstack = new LocalStackContainer(
			DockerImageName.parse("localstack/localstack:latest")).withServices(LocalStackContainer.Service.S3);

	static {
		localstack.start();
	}

	S3Client s3;

	@Autowired
	S3IngestionJob s3IngestionJob;

	@Autowired
	ReviewRepository reviewRepository;

	@DynamicPropertySource
	static void s3Props(DynamicPropertyRegistry r) {
		r.add("aws.s3.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
		r.add("aws.region", localstack::getRegion); // usually us-east-1
		r.add("aws.creds.accessKey", localstack::getAccessKey);
		r.add("aws.creds.secretKey", localstack::getSecretKey);
		r.add("aws.s3.path-style", () -> "true"); // important for LocalStack
		r.add("spring.task.scheduling.enabled", () -> "false");
	}

	@BeforeAll
	void startLocalstack() {
		s3 = S3Client.builder().endpointOverride(localstack.getEndpointOverride(S3))
				.region(Region.of(localstack.getRegion()))
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();

		ensureBucket(s3, BUCKET, localstack.getRegion());
	}

	private static void ensureBucket(S3Client s3, String bucket, String region) {
		try {
			s3.headBucket(b -> b.bucket(bucket));
		} catch (NoSuchBucketException e) {
			if (!"us-east-1".equals(region)) {
				s3.createBucket(b -> b.bucket(bucket).createBucketConfiguration(c -> c.locationConstraint(region)));
			} else {
				s3.createBucket(b -> b.bucket(bucket));
			}
		}
	}

	@BeforeEach
	void seedData() throws Exception {
		log.info("seedData Start");

		var res = new ClassPathResource("input/review.jl");
		String json = StreamUtils.copyToString(res.getInputStream(), StandardCharsets.UTF_8);

		// upload to LocalStack S3 where your job expects it
		s3.putObject(b -> b.bucket(BUCKET).key("input/review.jl").contentType("application/x-ndjson"),
				RequestBody.fromString(json, StandardCharsets.UTF_8));

		var list = s3.listObjectsV2(b -> b.bucket(BUCKET).prefix("input/"));
		boolean found = list.contents().stream().anyMatch(o -> o.key().equals("input/review.jl"));

		Assertions.assertTrue(found, "Object input/review.jl not found under input/");

		log.info("App S3 sees {} objects under input/: {}", list.hasContents() ? list.contents().size() : 0,
				list.hasContents() ? list.contents().stream().map(S3Object::key).toList() : List.of());

	}

	@Test
	void importsAndPersistsOnce() {
		log.info("imports_and_persists_once Start");
		long before = reviewRepository.count();
		s3IngestionJob.pollOnce();
		// assert DB state
		assertThat(reviewRepository.count()).isEqualTo(before + 1);
		var review = reviewRepository.findAll().get((int) before); // grab the newly inserted
		assertThat(review.getHotel().getName()).isEqualTo("Oscar Saigon Hotel");
		assertThat(review.getProvider().getName()).isEqualTo("Agoda");
		assertThat(review.getRating()).isEqualTo(6.4);
	}

	@AfterEach
	void cleanObjects() {
		ListObjectsV2Response list = s3.listObjectsV2(b -> b.bucket(BUCKET));
		for (S3Object o : list.contents()) {
			s3.deleteObject(b -> b.bucket(BUCKET).key(o.key()));
		}
	}

	@AfterAll
	void teardown() {
		s3.deleteBucket(b -> b.bucket(BUCKET));
		localstack.stop(); // container halts automatically in CI too
	}
}