package com.liah.doribottle.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient


@Configuration
class AwsConfig(
    @Value("\${spring.cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${spring.cloud.aws.credentials.secret-key}") private val secretKey: String
) {
    /**
     * SQS
     */

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.AP_NORTHEAST_2)
            .build()
    }

    @Bean
    fun defaultSqsListenerContainerFactory(): SqsMessageListenerContainerFactory<Any> {
        return SqsMessageListenerContainerFactory
            .builder<Any>()
            .sqsAsyncClient(sqsAsyncClient())
            .build()
    }

    @Bean
    fun sqsTemplate(): SqsTemplate {
        return SqsTemplate.newTemplate(sqsAsyncClient())
    }

    /**
     * S3
     */

    @Bean
    fun amazonS3(): AmazonS3 {
        val credentials: AWSCredentials = BasicAWSCredentials(accessKey, secretKey)

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_NORTHEAST_2)
            .build()
    }
}