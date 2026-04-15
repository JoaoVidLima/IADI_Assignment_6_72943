package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.repository.EventRepository

@Component("eventSecurity")
class EventSecurity(
    private val eventRepository: EventRepository
) {

    // Returns true if the authenticated user is the event owner
    fun isOwner(eventId: Long, authentication: Authentication): Boolean {
        val event = eventRepository.findById(eventId).orElse(null) ?: return false
        return event.createdBy?.username == authentication.name
    }

    // Returns true if the authenticated user is the owner OR has ROLE_ADMIN
    fun isOwnerOrAdmin(eventId: Long, authentication: Authentication): Boolean {
        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        return isAdmin || isOwner(eventId, authentication)
    }
}