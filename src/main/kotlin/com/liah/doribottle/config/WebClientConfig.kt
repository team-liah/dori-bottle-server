package com.liah.doribottle.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {
    private val om =
        jacksonObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false,
        ).registerModules(JavaTimeModule())

    @Bean
    fun webClient(
        httpClient: HttpClient,
        exchangeStrategies: ExchangeStrategies,
    ): WebClient {
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(exchangeStrategies)
            .build()
    }

    @Bean
    fun httpClient(connectionProvider: ConnectionProvider): HttpClient {
        return HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(5))
                    .addHandlerLast(WriteTimeoutHandler(5))
            }
    }

    @Bean
    fun connectionProvider(): ConnectionProvider {
        return ConnectionProvider.builder("http-pool")
            .maxConnections(100)
            .pendingAcquireMaxCount(-1)
            .pendingAcquireTimeout(Duration.ofMillis(0))
            .maxIdleTime(Duration.ofMillis(2000))
            .build()
    }

    @Bean
    fun exchangeStrategies(): ExchangeStrategies {
        return ExchangeStrategies.builder()
            .codecs { config ->
                config.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(om, MediaType.APPLICATION_JSON))
                config.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(om, MediaType.APPLICATION_JSON))
                config.defaultCodecs().maxInMemorySize(1024 * 1024)
            }
            .build()
    }
}
