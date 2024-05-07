package com.liah.doribottle.apiclient

import com.liah.doribottle.apiclient.vm.SlackMessageDomain
import com.liah.doribottle.apiclient.vm.SlackMessageSendRequest
import com.liah.doribottle.apiclient.vm.SlackMessageType
import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.machine.dto.MachineDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SlackApiClient(
    appProperties: AppProperties,
    webClient: WebClient,
) : BaseApiClient(webClient) {
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

        retrievePostForMono(
            uri = webhookUrl,
            requestBody = request,
            responseType = String::class.java,
        ).subscribe { response ->
            log.info("Slack message delivery status: $response")
        }
    }
}
