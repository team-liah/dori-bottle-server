package com.liah.doribottle.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val auth: AuthProperties,
    val naverCloud: NaverCloud,
    val tosspayments: Tosspayments,
    val aws: Aws
) {
    data class AuthProperties(
        val jwt: Jwt,
        val refreshJwt: RefreshJwt,
    ) {
        data class Jwt(
            val base64Secret: String,
            val expiredMs: Long,
            val preAuthExpiredMs: Long
        )

        data class RefreshJwt(
            val expiredMs: Long
        )
    }

    data class NaverCloud(
        val notification: Notification,
    ) {
        data class Notification(
            val sms: Sms
        ) {
            data class Sms(
                val baseUrl: String,
                val serviceId: String,
            )
        }
    }

    data class Tosspayments(
        val baseUrl: String,
        val secretKey: String
    )

    data class Aws(
        val sqs: Sqs,
        val s3: S3
    ) {
        data class Sqs(
            val queueName: String
        )

        data class S3(
            val bucketName: String
        )
    }
}