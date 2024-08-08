package com.example.cashcardkotlin.user

import com.example.cashcardkotlin.cashcard.models.CashCard
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
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val cashcards: Set<CashCard> = mutableSetOf(),
    @Enumerated(value = EnumType.STRING)
    val role: Role? = Role.USER,
    private val password: String? = null,
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(this.role?.name))
    }

    override fun getPassword(): String {
        return password!!
    }

    override fun getUsername(): String {
        return email!!
    }

    override fun toString(): String {
        return "User(id=$id, email='$email', firstname='$firstname', lastname='$lastname', email='$telephone' )"
    }

    fun toUserDto(): UserDto {
        return UserDto(id, email, firstname, lastname, telephone)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

enum class Role {
    USER,
    OWNER,
    ADMIN,
}
