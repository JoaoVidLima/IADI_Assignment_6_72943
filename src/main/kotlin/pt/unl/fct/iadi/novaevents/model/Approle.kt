package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "app_role")
class AppRole(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser = AppUser(),

    @Column(nullable = false)
    var roleName: String = ""  // e.g. "ROLE_EDITOR", "ROLE_ADMIN"
)