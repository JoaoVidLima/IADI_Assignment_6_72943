package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

@Repository
interface EventRepository : JpaRepository<Event, Long> {

    //Perform event filter (Not used because it forces all the filters to be used. Version with jpql to handle it bellow)
    fun findByEventTypeAndClubAndDateBetween(eventType: EventType, club: Club, from: LocalDate, to: LocalDate): List<Event>

    //Using JPQL
    @Query("""
    SELECT e FROM Event e
    JOIN FETCH e.club
    JOIN FETCH e.eventType
    WHERE (:clubId IS NULL OR e.club.id = :clubId)
    AND (:typeId IS NULL OR e.eventType.id = :typeId)
    AND (:from IS NULL OR e.date >= :from)
    AND (:to IS NULL OR e.date <= :to)
    """)
    fun findFilteredEvents(
        @Param("clubId") clubId: Long?,
        @Param("typeId") typeId: Long?,
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?
    ): List<Event>

    // Check for duplicate names
    fun existsByNameIgnoreCase(name: String): Boolean

    // Check for duplicates but exclude the current ID (used for Updates)
    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    @Query("SELECT e FROM Event e JOIN FETCH e.eventType JOIN FETCH e.club WHERE e.club.id = :clubId")
    fun findByClubIdWithDetails(@Param("clubId") clubId: Long): List<Event>

}