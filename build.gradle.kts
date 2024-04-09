import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
	kotlin("plugin.jpa") version "1.9.20"
	kotlin("plugin.allopen") version "1.9.20"
	kotlin("plugin.noarg") version "1.9.20"
	kotlin("kapt") version "1.9.20"
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

noArg {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

group = "com.liah"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.security:spring-security-test")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")

	// aws
	implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
	implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.0.1"))
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// testcontainers
	testImplementation("org.testcontainers:testcontainers:1.19.3")
	testImplementation("org.testcontainers:mariadb:1.19.3")

	// ulid-creator
	implementation("com.github.f4b6a3:ulid-creator:5.2.0")

	// jjwt-api
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// jackson-module-kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

	// query dsl
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	implementation("com.querydsl:querydsl-core:5.0.0")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
	kapt("com.querydsl:querydsl-kotlin-codegen:5.0.0")

	// shedlock
	implementation("net.javacrumbs.shedlock:shedlock-spring:5.10.0")
	implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.10.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}