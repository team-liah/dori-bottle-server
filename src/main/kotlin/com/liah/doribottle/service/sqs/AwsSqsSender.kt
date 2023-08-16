package com.liah.doribottle.service.sqs

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AwsSqsSender(
    private val sqsTemplate: SqsTemplate,
    @Value("\${app.aws.sqs.queueName}") private val queueName: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(message: PointSaveMessage) {
        sqsTemplate.send<PointSaveMessage> { to -> to
            .queue(queueName)
            .payload(message)
        }

        log.info("Send point-save-message : $message")
    }
}