package com.example.cashcardkotlin

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val telephone: String? = null,
    private val password: String? = null,
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {

        return mutableListOf()

    }
    override fun getPassword(): String {
       return password!!
    }

    override fun getUsername(): String {
        return email!!
    }
}
