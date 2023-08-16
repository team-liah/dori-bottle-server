package com.liah.doribottle.service.sqs

import com.liah.doribottle.service.point.PointService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AwsSqsListener (
    private val pointService: PointService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("\${app.aws.sqs.queueName}")
    fun listen(message: PointSaveMessage) {
        log.info("Receive point-save-message : $message")
        pointService.save(message.userId!!, message.saveType!!, message.eventType!!, message.saveAmounts!!)
    }
}