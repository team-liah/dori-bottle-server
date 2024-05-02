package com.liah.doribottle.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val web: Web,
    val auth: AuthProperties,
    val ncloud: Ncloud,
    val tosspayments: Tosspayments,
    val aws: Aws,
    val slack: Slack,
) {
    data class Web(
        val baseUrl: String,
        val adminBaseUrl: String,
    )

    data class AuthProperties(
        val jwt: Jwt,
        val refreshJwt: RefreshJwt,
    ) {
        data class Jwt(
            val base64Secret: String,
            val expiredMs: Long,
            val preAuthExpiredMs: Long,
            val systemExpiredMs: Long,
        )

        data class RefreshJwt(
            val expiredMs: Long,
        )
    }

    data class Ncloud(
        val baseUrl: String,
        val accessKey: String,
        val secretKey: String,
        val notification: Notification,
    ) {
        data class Notification(
            val sms: Sms,
        ) {
            data class Sms(
                val servicePath: String,
                val serviceId: String,
                val callingNumber: String,
            )
        }
    }

    data class Tosspayments(
        val baseUrl: String,
        val secretKey: String,
    )

    data class Aws(
        val sqs: Sqs,
        val s3: S3,
    ) {
        data class Sqs(
            val queueName: String,
        )

        data class S3(
            val bucketName: String,
        )
    }

    data class Slack(
        val webhookUrl: String,
    )
}
