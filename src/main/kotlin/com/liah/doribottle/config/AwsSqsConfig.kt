package com.liah.doribottle.config

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
class AwsSqsConfig(
    @Value("\${spring.cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${spring.cloud.aws.credentials.secret-key}") private val secretKey: String,
) {
    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        return SqsAsyncClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.AP_NORTHEAST_2)
            .build()
    }

    @Bean
    fun defaultSqlListenerContainerFactory(): SqsMessageListenerContainerFactory<Any> {
        return SqsMessageListenerContainerFactory
            .builder<Any>()
            .sqsAsyncClient(sqsAsyncClient())
            .build()
    }

    @Bean
    fun sqsTemplate(): SqsTemplate {
        return SqsTemplate.newTemplate(sqsAsyncClient())
    }
}