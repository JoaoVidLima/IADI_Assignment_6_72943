package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Event(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club = Club(),

    @Column(name = "name", unique = true, nullable = false)
    var name: String = "",

    @Column(name = "date", nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(name = "location")
    var location: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    var eventType: EventType = EventType(),

    @Column(name = "description", length = 2000)
    var description: String? = null,

    // Nullable — seeded events have no owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    var createdBy: AppUser? = null
)