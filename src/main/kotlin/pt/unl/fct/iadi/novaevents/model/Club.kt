package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Club(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long = 0,

    @Column(name = "name", unique = true, nullable = false)
    var name: String = "",

    @Column(name = "description", length = 2000, nullable = false)
    var description: String? = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    var category: ClubCategory = ClubCategory.DEFAULT
) {
    enum class ClubCategory {
        TECHNOLOGY, ARTS, SPORTS, ACADEMIC, SOCIAL, CULTURAL, DEFAULT
    }

    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    var events: MutableList<Event>? = mutableListOf()
}



