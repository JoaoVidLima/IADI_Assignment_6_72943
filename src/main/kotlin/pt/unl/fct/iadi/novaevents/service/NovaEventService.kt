package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.bookstore.service.ClubNotFoundException
import pt.unl.fct.iadi.bookstore.service.EventAlreadyExistsException
import pt.unl.fct.iadi.bookstore.service.EventNotFoundException
import pt.unl.fct.iadi.novaevents.controller.dto.EventFilter
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import java.time.LocalDate

@Service
class NovaEventService(
    private val eventTypeRepository: EventTypeRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository
) {

    fun listAllClubs(): List<Club> {
        return clubRepository.findAll()
    }

    fun getAllClubsWithEventCount(): Map<Long, Long> {
        val res: List<Array<Any>> = clubRepository.countEventsPerClub()
        return res.associate { row -> (row[0] as Long) to (row[1] as Long) }
    }

    fun getClubById(id: Long): Club {
        return clubRepository.findById(id).orElseThrow {
            ClubNotFoundException(id)
        }
    }

    @Transactional
    fun getClubWithEvents(clubId: Long): Pair<Club, List<Event>> {
        val club = getClubById(clubId)
        val events = club.events?.toList() ?: emptyList()
        return club to events
    }

    fun findEvents(filter: EventFilter): List<Event> {
        val typeId: Long? = filter.type?.let { eventTypeRepository.findByName(it) }?.id
        return eventRepository.findFilteredEvents(filter.clubId, typeId, filter.from, filter.to)
    }

    fun getEventById(id: Long): Event {
        return eventRepository.findById(id).orElseThrow {
            EventNotFoundException(id)
        }
    }

    // owner is the authenticated AppUser — resolved in the controller from principal.name
    fun createEvent(
        clubId: Long,
        name: String,
        date: LocalDate,
        location: String? = null,
        type: EventType,
        description: String? = null,
        owner: AppUser
    ): Event {
        if (eventRepository.existsByNameIgnoreCase(name)) {
            throw EventAlreadyExistsException(name)
        }
        val club = getClubById(clubId)
        return eventRepository.save(
            Event(
                club = club,
                name = name,
                date = date,
                location = location,
                eventType = type,
                description = description,
                createdBy = owner
            )
        )
    }

    @Transactional
    fun updateEvent(
        eventId: Long,
        name: String,
        date: LocalDate,
        location: String? = null,
        type: EventType,
        description: String? = null
    ): Event {
        val existing = getEventById(eventId)

        if (eventRepository.existsByNameIgnoreCaseAndIdNot(name, eventId)) {
            throw EventAlreadyExistsException(name)
        }

        existing.name = name
        existing.date = date
        existing.location = location
        existing.eventType = type
        existing.description = description

        return existing
    }

    fun deleteEvent(eventId: Long) {
        if (!eventRepository.existsById(eventId))
            throw EventNotFoundException(eventId)
        eventRepository.deleteById(eventId)
    }
}