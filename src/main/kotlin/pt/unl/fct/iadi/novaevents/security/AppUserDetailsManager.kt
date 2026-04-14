package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository

@Service
class AppUserDetailsManager(
    private val userRepository: AppUserRepository
) : UserDetailsManager {

    // Called by Spring Security on every login attempt.
    // Load user from DB, map to UserDetails (username, hashed password, roles).
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)
        return User(
            user.username,
            user.password,
            user.roles.map { SimpleGrantedAuthority(it.roleName) }
        )
    }

    // Called by DataInitializer to seed users. Password must already be BCrypt-encoded.
    override fun createUser(details: UserDetails) {
        val user = AppUser(
            username = details.username,
            password = details.password
        )
        user.roles = details.authorities
            .map { AppRole(user = user, roleName = it.authority) }
            .toMutableList()
        userRepository.save(user)
    }

    override fun userExists(username: String): Boolean =
        userRepository.existsByUsername(username)

    // Not needed for this project
    override fun updateUser(user: UserDetails) = Unit
    override fun deleteUser(username: String) = Unit
    override fun changePassword(oldPassword: String, newPassword: String) = Unit

    // Helper to get the actual AppUser domain object (needed for setting createdBy on Event)
    fun findDomainUser(username: String): AppUser =
        userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)
}