package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import pt.unl.fct.iadi.novaevents.model.Club

@Repository
interface ClubRepository: JpaRepository<Club, Long> {

    @Query("""
        SELECT c.id, COUNT(e) FROM Club c LEFT JOIN c.events e GROUP BY c.id
    """)
    fun countEventsPerClub(): List<Array<Any>> //Each array in the List represents a row. Example [ [1, 3], [2, 4], [3, 2]]

}