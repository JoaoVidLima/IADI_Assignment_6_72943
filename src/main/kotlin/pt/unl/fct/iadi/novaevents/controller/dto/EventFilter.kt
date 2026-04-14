package pt.unl.fct.iadi.novaevents.controller.dto

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class EventFilter(
    var type: String? = null,
    var clubId: Long? = null,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var from: LocalDate? = null,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var to: LocalDate? = null
)
