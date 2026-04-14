package pt.unl.fct.iadi.novaevents.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

data class CreateEventRequest(
    @field:NotBlank(message = "Name is required")
    var name: String = "",
    @field:NotNull(message = "Date is required")
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var date: LocalDate? = null,
    @field:NotNull(message = "Event type is required")
    var type: String? = null,
    var location: String? = null,
    var description: String? = null
)
