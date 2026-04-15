package pt.unl.fct.iadi.novaevents.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import pt.unl.fct.iadi.bookstore.service.EventAlreadyExistsException
import pt.unl.fct.iadi.novaevents.controller.dto.CreateEventRequest
import pt.unl.fct.iadi.novaevents.controller.dto.EditEventRequest
import pt.unl.fct.iadi.novaevents.controller.dto.EventFilter
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import pt.unl.fct.iadi.novaevents.security.AppUserDetailsManager
import pt.unl.fct.iadi.novaevents.service.NovaEventService
import java.security.Principal
import java.time.LocalDate

@Controller
class NovaEventsController(
    private var service: NovaEventService,
    private val eventTypeRepository: EventTypeRepository,
    private val userDetailsManager: AppUserDetailsManager
) : NovaEventsAPI {

    override fun login(): String {
        return "login" // Returns login.html template
    }

    override fun listClubs(model: Model): String {
        val counts = service.getAllClubsWithEventCount()
        model.addAttribute("clubs", service.listAllClubs())
        model.addAttribute("eventCounter", counts)
        return "clubs/list"
    }

    override fun viewClubDetails(id: Long, model: Model): String {
        val (club, clubEvents) = service.getClubWithEvents(id)
        model.addAttribute("club", club)
        model.addAttribute("clubEvents", clubEvents)
        return "clubs/details"
    }

    override fun listAllEvents(filter: EventFilter, model: Model): String {
        val events = service.findEvents(filter)
        val allClubs = service.listAllClubs()
        model.addAttribute("events", events)
        model.addAttribute("filter", filter)
        model.addAttribute("eventTypes", eventTypeRepository.findAll())
        model.addAttribute("allClubs", allClubs)
        return "events/list"
    }

    override fun viewEventDetails(clubId: Long, eventId: Long, model: Model): String {
        val event: Event = service.getEventById(eventId)
        val club: Club = service.getClubById(clubId)
        model.addAttribute("event", event)
        model.addAttribute("clubName", club.name)
        return "events/details"
    }

    override fun showCreateEventForm(clubId: Long, model: Model): String {
        val club: Club = service.getClubById(clubId)
        model.addAttribute("club", club)
        model.addAttribute("request", CreateEventRequest())
        model.addAttribute("eventTypes", eventTypeRepository.findAll())
        return "events/create"
    }

    override fun createEvent(
        clubId: Long, request: CreateEventRequest,
        bindingResult: BindingResult, model: Model,
        principal: Principal  // injected by Spring — the logged-in user
    ): String {
        if (bindingResult.hasErrors()) {
            return reRenderCreateEvent(clubId, model)
        }

        try {
            val eventType: EventType = eventTypeRepository.findByName(request.type!!)
                ?: throw IllegalArgumentException("Invalid event type: ${request.type}")

            val owner = userDetailsManager.findDomainUser(principal.name)

            val eventCreated: Event = service.createEvent(
                clubId, request.name, request.date!!, request.location, eventType,
                request.description, owner
            )
            return "redirect:/clubs/$clubId/events/${eventCreated.id}"

        } catch (ex: EventAlreadyExistsException) {
            bindingResult.rejectValue("name", "duplicate", "An event with this name already exists")
            return reRenderCreateEvent(clubId, model)
        }
    }

    fun reRenderCreateEvent(clubId: Long, model: Model): String {
        val club: Club = service.getClubById(clubId)
        model.addAttribute("club", club)
        model.addAttribute("eventTypes", eventTypeRepository.findAll())
        return "events/create"
    }

    // Only the owner can see/use the edit form
    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication)")
    override fun showEditEventForm(clubId: Long, eventId: Long, model: Model): String {
        val club: Club = service.getClubById(clubId)
        val event: Event = service.getEventById(eventId)
        val request = EditEventRequest(
            name = event.name,
            date = event.date,
            type = event.eventType.name,
            location = event.location,
            description = event.description
        )
        model.addAttribute("club", club)
        model.addAttribute("event", event)
        model.addAttribute("request", request)
        model.addAttribute("eventTypes", eventTypeRepository.findAll())
        return "events/edit"
    }

    // Only the owner can submit the edit
    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication)")
    override fun editEventDetails(
        clubId: Long, eventId: Long, request: EditEventRequest,
        bindingResult: BindingResult, model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            return reRenderEditEvent(clubId, eventId, model)
        }

        try {
            val eventType: EventType = eventTypeRepository.findByName(request.type!!)
                ?: throw IllegalArgumentException("Invalid event type: ${request.type}")

            val eventEdited: Event = service.updateEvent(
                eventId, request.name, request.date!!, request.location, eventType,
                request.description
            )
            return "redirect:/clubs/$clubId/events/${eventEdited.id}"

        } catch (ex: EventAlreadyExistsException) {
            bindingResult.rejectValue("name", "duplicate", "An event with this name already exists")
            return reRenderEditEvent(clubId, eventId, model)
        }
    }

    fun reRenderEditEvent(clubId: Long, eventId: Long, model: Model): String {
        val club = service.getClubById(clubId)
        val event = service.getEventById(eventId)
        model.addAttribute("club", club)
        model.addAttribute("event", event)
        model.addAttribute("eventTypes", eventTypeRepository.findAll())
        return "events/edit"
    }

    // Owner or admin can see the delete confirmation
    @PreAuthorize("@eventSecurity.isOwnerOrAdmin(#eventId, authentication)")
    override fun deleteEventConfirmation(clubId: Long, eventId: Long, model: Model): String {
        val event: Event = service.getEventById(eventId)
        model.addAttribute("event", event)
        return "events/delete"
    }

    // Owner or admin can actually delete
    @PreAuthorize("@eventSecurity.isOwnerOrAdmin(#eventId, authentication)")
    override fun deleteEvent(clubId: Long, eventId: Long): String {
        service.deleteEvent(eventId)
        return "redirect:/clubs/$clubId"
    }
}