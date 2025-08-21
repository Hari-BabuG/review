Review Service::

	A Spring Boot–based service that ingests hotel reviews from S3, parses them from .jl files, and 	persists them into PostgreSQL.
	Built with containerized services and Redis caching for performance.

Features::

	1) Spring Boot application with Flyway-managed migrations.
	
	2) PostgreSQL for persistence.
	
	3) S3 ingestion job to fetch and process review files.
	
	4) Redis caching for idempotent ingestion and reduced DB lookups.
	
	5) Integration tests using H2 (Postgres mode).
	
	6) Docker Compose setup for local development with Localstack (mock S3), Postgres, Redis.

Requirements::

	1) Java 17+
	
	2) Maven 3.8+
	
	3) Docker & Docker Compose

Build & Run::

	1. Build the JAR
	mvn clean install
	
	2) Bring up the service
	docker compose up -d db localstack redis
	
	3) Start App
	docker compose up --build app
	
	
	Steps 2) & 3)  will start:
	
	# db → PostgreSQL
	
	# localstack → Mock AWS S3
	
	# redis → Redis cache
	
	# s3-setup → Bootstrap bucket and sample .jl file
	
	# review-service → Spring Boot application

Configuration

Application properties are externalized via application.yml and .env.

	Example .env
		AWS_ACCESS_KEY_ID=dummy
		AWS_SECRET_ACCESS_KEY=dummy
		AWS_REGION=ap-south-1
		S3_BUCKET=reviews-bucket
		S3_PREFIX=daily/
		API_KEY=demo-key
		INGEST_DELAY_MS=2000
		INGEST_PARALLELISM=4
		APP_INGESTION_SCHEDULE=PT2M

Caching::

	1) Uses Redis (spring.cache.type=redis).
	
	2) Cache name: recentS3Keys.
	
	3) Ensures files already processed are skipped quickly without redundant DB hits.

Development Notes::

	1) Flyway manages schema migrations (src/main/resources/db/migration).
	
	2) H2 with PostgreSQL compatibility is used in tests.
	
	3) Ingestion job runs every 2 minutes (@Scheduled) or can be triggered manually.

Demo Cleanup::

	To reset demo state (remove old files and DB state):

	docker compose down -v
	docker compose up -d db localstack redis
	docker compose up --build app