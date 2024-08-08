package com.example.cashcardkotlin.config

import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager


@Configuration
class AppConfig(val userRepository: UserRepository) {

    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username: String? ->
            println("userdetailsservice: username: $username")
            userRepository.findByEmail(username!!) ?: throw UsernameNotFoundException("User not found")
        }
    }

//        @Bean
//    fun testOnlyUser(passwordEncoder: PasswordEncoder): UserDetailsService {
////        val userBuilder = User.builder()
//        val john = User(
//            firstname = "John",
//            lastname = "Smith",
//            role = Role.OWNER,
//            password = "Test@123",
//            email = "owner@xyz.com",
//            telephone = "123456789"
//        )
//
////        val sarah = userBuilder
////            .username("sarah1")
////            .password(passwordEncoder.encode("abc123"))
////            .roles("CARD-OWNER")
////            .build()
////
////        val hankOwnNoCards = userBuilder
////            .username("hank-owns-no-cards")
////            .password(passwordEncoder.encode("qrs456"))
////            .roles("NON-OWNER")
////            .build()
////
////        val kumar = userBuilder
////            .username("kumar2")
////            .password(passwordEncoder.encode("xyz789"))
////            .roles("CARD-OWNER")
////            .build()
//        return InMemoryUserDetailsManager(john)
//    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

}