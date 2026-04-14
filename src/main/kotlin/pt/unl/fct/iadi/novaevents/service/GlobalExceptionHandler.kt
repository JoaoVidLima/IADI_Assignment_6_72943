package pt.unl.fct.iadi.novaevents.service

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import pt.unl.fct.iadi.bookstore.service.ClubNotFoundException
import pt.unl.fct.iadi.bookstore.service.EventNotFoundException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ClubNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun clubNotFound(ex: ClubNotFoundException, model: Model): String{
        model.addAttribute("errorMessage", "404: Club with ID ${ex.id} was not found.")
        return "error/errorPage"
    }

    @ExceptionHandler(EventNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun eventNotFound(ex: EventNotFoundException, model: Model): String{
        model.addAttribute("errorMessage", "404: Event with ID ${ex.id} was not found.")
        return "error/errorPage"
    }


}