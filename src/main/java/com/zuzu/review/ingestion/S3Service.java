package com.zuzu.review.ingestion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

//Spring Retry
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.EnableRetry;

//Java
import java.io.IOException;


@Slf4j
@EnableRetry
@Service
@RequiredArgsConstructor
public class S3Service {
	
	private final S3Client s3;

	@Value("${aws.s3.bucket}")
	private String bucket;
	@Value("${aws.s3.prefix}")
	private String prefix;
	
	@Retryable(
		    value = {SdkClientException.class, IOException.class},
		    maxAttempts = 2,
		    backoff = @Backoff(delay = 500, multiplier = 2, random = true))
	public List<S3Object> listNewObjects() {
		log.info("listNewObjects Start");
		List<S3Object> results = new ArrayList<>();
		String token = null;
		do {
			ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix)
					.continuationToken(token).build();
			log.info("listNewObjects bucket: {} prefix: {} ",bucket,prefix);
			ListObjectsV2Response resp = s3.listObjectsV2(req);
			for (S3Object obj : resp.contents()) {
				if (!obj.key().endsWith(".jl"))
					continue;
				results.add(obj);
			}
			token = resp.nextContinuationToken();
		} while (token != null);
		log.info("listNewObjects End");
		return results;
	}
	
	@Retryable(
		    value = {SdkClientException.class, IOException.class},
		    maxAttempts = 2,
		    backoff = @Backoff(delay = 500, multiplier = 2, random = true))
	public BufferedReader openObject(S3Object obj) {
		GetObjectRequest get = GetObjectRequest.builder().bucket(bucket).key(obj.key()).build();
		ResponseInputStream<GetObjectResponse> is = s3.getObject(get);
		return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	}
}
