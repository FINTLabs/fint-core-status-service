package no.fintlabs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import javax.management.RuntimeMBeanException

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {


    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    val idpUri: String = ""

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange{ it.anyExchange().authenticated() }
            .oauth2ResourceServer{ it.jwt {
                this::validateAndConvertJwt
            } }
            .build()

    }

    fun validateAndConvertJwt(jwt: Jwt): Mono<Authentication> {
        if (jwt.issuer.equals(idpUri))
            return Mono.just(JwtAuthenticationToken(jwt))
        else return Mono.error(RuntimeException())
    }

}