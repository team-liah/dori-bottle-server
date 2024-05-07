package com.liah.doribottle.apiclient

import com.liah.doribottle.common.error.exception.BadWebClientRequestException
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

abstract class BaseApiClient(
    private val webClient: WebClient,
) {
    protected fun <T> retrievePostForMono(
        uri: String,
        headers: MultiValueMap<String, String>? = null,
        requestBody: Any,
        responseType: Class<T>,
    ): Mono<T> {
        return webClient
            .post()
            .uri(uri)
            .headers { httpHeaders ->
                headers?.let { httpHeaders.addAll(it) }
            }
            .bodyValue(requestBody)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }, { response ->
                Mono.error(
                    BadWebClientRequestException(
                        statusCode = response.statusCode().value(),
                        statusText =
                            String.format(
                                "4xx 외부 요청 오류. StatusCode: %s, ResponseBody: %s, Header: %s",
                                response.statusCode().value(),
                                response.bodyToMono(String::class.java),
                                response.headers().asHttpHeaders(),
                            ),
                    ),
                )
            })
            .onStatus({ status -> status.is5xxServerError }, { response ->
                Mono.error(
                    WebClientResponseException(
                        response.statusCode().value(),
                        String.format(
                            "5xx 외부 요청 오류. %s",
                            response.bodyToMono(String::class.java),
                        ),
                        response.headers().asHttpHeaders(),
                        null,
                        null,
                    ),
                )
            })
            .bodyToMono(responseType)
    }
}
