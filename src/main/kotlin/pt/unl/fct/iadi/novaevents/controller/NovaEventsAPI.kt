package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import pt.unl.fct.iadi.novaevents.controller.dto.CreateEventRequest
import pt.unl.fct.iadi.novaevents.controller.dto.EditEventRequest
import pt.unl.fct.iadi.novaevents.controller.dto.EventFilter

interface NovaEventsAPI {

    // STORY 1: List all clubs
    @GetMapping("/clubs")
    fun listClubs(model: Model): String

    //------------------------------------------------------------------------------------------------------------------

    // STORY 2: View club details
    @GetMapping("/clubs/{id}")
    fun viewClubDetails(@PathVariable id: Long, model: Model): String

    //------------------------------------------------------------------------------------------------------------------

    // STORY 3: List all events (filtered list)
    @GetMapping("/events")
    fun listAllEvents(@ModelAttribute("filter") filter: EventFilter, model: Model): String

    //------------------------------------------------------------------------------------------------------------------

    // STORY 4: View event details
    @GetMapping("/clubs/{clubId}/events/{eventId}")
    fun viewEventDetails(@PathVariable clubId: Long, @PathVariable eventId: Long, model: Model): String

    //------------------------------------------------------------------------------------------------------------------

    // STORY 5: Create Event (The Form + The Action)
    @GetMapping("/clubs/{clubId}/events/new")
    fun showCreateEventForm(@PathVariable clubId: Long, model: Model): String

    @PostMapping("/clubs/{clubId}/events")
    fun createEvent(@PathVariable clubId: Long, @Valid @ModelAttribute("request") request: CreateEventRequest,
                    bindingResult: BindingResult, model: Model): String //BindingResult must go exactly after request

    //------------------------------------------------------------------------------------------------------------------

    // STORY 6: Edit Event (The Form + The Action)
    @GetMapping("/clubs/{clubId}/events/{eventId}/edit")
    fun showEditEventForm(@PathVariable clubId: Long, @PathVariable eventId: Long, model: Model): String

    @PutMapping("/clubs/{clubId}/events/{eventId}")
    fun editEventDetails(@PathVariable clubId: Long, @PathVariable eventId: Long,
                         @Valid @ModelAttribute("request") request: EditEventRequest,
                         bindingResult: BindingResult, model: Model): String

    //------------------------------------------------------------------------------------------------------------------

    // STORY 7: Delete Event
    @GetMapping("/clubs/{clubId}/events/{eventId}/delete")
    fun deleteEventConfirmation(@PathVariable clubId: Long, @PathVariable eventId: Long, model: Model): String

    @DeleteMapping("/clubs/{clubId}/events/{eventId}")
    fun deleteEvent(@PathVariable clubId: Long, @PathVariable eventId: Long): String



}