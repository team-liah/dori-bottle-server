package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenProvider: TokenProvider
) {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { authorize -> authorize
                .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                .requestMatchers("/api/v1/account/auth/send-sms", "/api/v1/account/auth", "/api/v1/account/refresh-auth", "/api/v1/account/logout").permitAll()
                .requestMatchers("/admin/api/account/auth", "/admin/api/account/logout").permitAll()
                .requestMatchers("/api/v1/account/register").hasRole(Role.GUEST.name)
                .requestMatchers("/api/v1/me").authenticated()
                .requestMatchers("/admin/**").hasAnyRole(Role.ADMIN.name, Role.MACHINE_ADMIN.name)
                .anyRequest().hasRole(Role.USER.name)
            }
            .exceptionHandling { exception -> exception
                .accessDeniedHandler(RestAccessDeniedHandler())
                .authenticationEntryPoint(RestAuthenticationEntryPoint())
            }

        return http.build()
    }
}