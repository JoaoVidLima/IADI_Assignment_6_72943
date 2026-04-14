package pt.unl.fct.iadi.bookstore.service

// Base class for all our domain errors
sealed class NovaEventsException(message: String) : RuntimeException(message)

class ClubNotFoundException(val id: Long) : NovaEventsException("Club with ID $id was not found.")

class EventNotFoundException(val id: Long) : NovaEventsException("Event with ID $id was not found.")

class EventAlreadyExistsException(val name: String) : NovaEventsException("Event with name '$name' already exists.")
