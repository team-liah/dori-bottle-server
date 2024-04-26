package com.liah.doribottle.service.sqs

import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.sqs.dto.PointSaveMessage
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AwsSqsSender(
    appProperties: AppProperties,
    private val sqsTemplate: SqsTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val queueName = appProperties.aws.sqs.queueName

    fun send(message: PointSaveMessage?) {
        message?.let {
            sqsTemplate.send<PointSaveMessage> { to -> to
                .queue(queueName)
                .payload(it)
            }

            log.info("Send point-save-message : $message")
        }
    }
}