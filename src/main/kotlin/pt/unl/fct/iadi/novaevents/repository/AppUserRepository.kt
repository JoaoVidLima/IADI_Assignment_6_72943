package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.iadi.novaevents.model.AppUser

@Repository
interface AppUserRepository : JpaRepository<AppUser, Long> {
    fun findByUsername(username: String): AppUser?
    fun existsByUsername(username: String): Boolean
}
