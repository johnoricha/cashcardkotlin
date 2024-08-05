package com.example.cashcardkotlin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(val authenticationProvider: AuthenticationProvider) {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests { request ->
            request.
                requestMatchers("auth/**")
                    .permitAll().
                requestMatchers("/cashcards/**")
                .hasAnyRole("CARD-OWNER", "ADMIN")
        }.httpBasic(Customizer.withDefaults())
            .authenticationProvider(authenticationProvider)
            .csrf { csrf -> csrf.disable() }.build()
    }

    @Bean
    fun testOnlyUser(passwordEncoder: PasswordEncoder): UserDetailsService {
        val userBuilder = User.builder()
        val john = userBuilder
            .username("john")
            .password(passwordEncoder.encode("Test@123"))
            .roles("ADMIN")
            .build()

        val sarah = userBuilder
            .username("sarah1")
            .password(passwordEncoder.encode("abc123"))
            .roles("CARD-OWNER")
            .build()

        val hankOwnNoCards = userBuilder
            .username("hank-owns-no-cards")
            .password(passwordEncoder.encode("qrs456"))
            .roles("NON-OWNER")
            .build()

        val kumar = userBuilder
            .username("kumar2")
            .password(passwordEncoder.encode("xyz789"))
            .roles("CARD-OWNER")
            .build()
        return InMemoryUserDetailsManager(john, sarah, hankOwnNoCards, kumar)
    }
}