package com.liah.doribottle.config.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .cors {  }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/v1/auth/join", "/api/v1/auth/token").permitAll()
                    .anyRequest().authenticated()
                // TODO: Separate by role
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(JwtFilter(secretKey), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}