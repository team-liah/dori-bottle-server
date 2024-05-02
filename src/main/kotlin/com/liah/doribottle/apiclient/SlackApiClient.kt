package com.liah.doribottle.apiclient

import com.liah.doribottle.apiclient.vm.SlackMessageDomain
import com.liah.doribottle.apiclient.vm.SlackMessageSendRequest
import com.liah.doribottle.apiclient.vm.SlackMessageType
import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.machine.dto.MachineDto
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SlackApiClient(
    appProperties: AppProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val adminBaseUrl = appProperties.web.adminBaseUrl
    private val webhookUrl = appProperties.slack.webhookUrl

    fun sendMessage(
        type: SlackMessageType,
        body: Any? = null,
    ) {
        val request =
            when (type.domain) {
                SlackMessageDomain.MACHINE -> {
                    SlackMessageSendRequest.forMachine(
                        type = type,
                        adminBaseUrl = adminBaseUrl,
                        machine = body as MachineDto,
                    )
                }
            }

        WebClient.create()
            .post()
            .uri(webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnError {
                log.error("Slack message was not delivered.")
            }
            .subscribe {
                log.info("Slack message delivery status: $it")
            }
    }
}
