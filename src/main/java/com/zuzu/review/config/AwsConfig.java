package com.zuzu.review.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class AwsConfig {

	//AWS SDK v2 S3 client, LocalStack-friendly (path-style + endpoint override)
	@Bean
	public S3Client s3Client(@org.springframework.beans.factory.annotation.Value("${aws.s3.endpoint:}") String endpoint,
			@org.springframework.beans.factory.annotation.Value("${aws.region}") String region,
			@org.springframework.beans.factory.annotation.Value("${aws.creds.accessKey}") String accessKey,
			@org.springframework.beans.factory.annotation.Value("${aws.creds.secretKey}") String secretKey,
			@org.springframework.beans.factory.annotation.Value("${aws.s3.path-style:true}") boolean pathStyle) {
		S3ClientBuilder b = S3Client.builder().region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyle) // <- important for
																									// LocalStack
						.build());

		if (endpoint != null && !endpoint.isBlank()) {
			b = b.endpointOverride(URI.create(endpoint));
		}
		return b.build();
	}
}
