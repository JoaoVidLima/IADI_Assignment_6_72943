package pt.unl.fct.iadi.novaevents.initializer

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import pt.unl.fct.iadi.novaevents.security.AppUserDetailsManager
import java.time.LocalDate

@Component
class DataInitializer(
    private val eventTypeRepository: EventTypeRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val userDetailsManager: UserDetailsManager,
    private val appUserDetailsManager: AppUserDetailsManager, // needed to resolve domain AppUser
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (!userDetailsManager.userExists("alice")) {
            listOf(
                User("alice", passwordEncoder.encode("password123"), listOf(SimpleGrantedAuthority("ROLE_EDITOR"))),
                User("bob", passwordEncoder.encode("password123"), listOf(SimpleGrantedAuthority("ROLE_EDITOR"))),
                User("charlie", passwordEncoder.encode("password123"), listOf(SimpleGrantedAuthority("ROLE_ADMIN"))),
            ).forEach { userDetailsManager.createUser(it) }
        }

        if (eventTypeRepository.count() > 0) return

        // charlie is the owner of all seeded events
        val charlie: AppUser = appUserDetailsManager.findDomainUser("charlie")

        // 1. Event types
        val workshop    = eventTypeRepository.save(EventType(name = "WORKSHOP"))
        val competition = eventTypeRepository.save(EventType(name = "COMPETITION"))
        val talk        = eventTypeRepository.save(EventType(name = "TALK"))
        val meeting     = eventTypeRepository.save(EventType(name = "MEETING"))
        val social      = eventTypeRepository.save(EventType(name = "SOCIAL"))
        val other       = eventTypeRepository.save(EventType(name = "OTHER"))

        // 2. Clubs
        val chess = clubRepository.save(Club(name = "Chess Club", description = "Strategy and focus. We welcome players of all levels.", category = Club.ClubCategory.SOCIAL))
        val robotics = clubRepository.save(Club(name = "Robotics Club", description = "The Robotics Club is the place to turn ideas into machines.", category = Club.ClubCategory.TECHNOLOGY))
        val photography = clubRepository.save(Club(name = "Photography Club", description = "Capturing moments, one frame at a time.", category = Club.ClubCategory.ARTS))
        val hiking = clubRepository.save(Club(name = "Hiking & Outdoors Club", description = "Explore nature and push your limits.", category = Club.ClubCategory.SPORTS))
        val film = clubRepository.save(Club(name = "Film Society", description = "Cinema lovers united by a passion for storytelling.", category = Club.ClubCategory.CULTURAL))

        // 3. Events — charlie is createdBy on all seeded events
        eventRepository.saveAll(listOf(
            Event(club = chess, name = "Beginner's Chess Workshop", date = LocalDate.of(2026, 3, 10), location = "Room A101", eventType = workshop, description = "Introduction to openings and basic tactics.", createdBy = charlie),
            Event(club = chess, name = "Spring Chess Tournament", date = LocalDate.of(2026, 4, 5), location = "Main Hall", eventType = competition, description = "Round-robin tournament open to all members.", createdBy = charlie),
            Event(club = chess, name = "Grandmaster Talk", date = LocalDate.of(2026, 5, 12), location = "Auditorium", eventType = talk, description = "Guest grandmaster shares career highlights.", createdBy = charlie),
            Event(club = robotics, name = "Arduino Crash Course", date = LocalDate.of(2026, 3, 15), location = "Lab 3", eventType = workshop, description = "Hands-on intro to Arduino microcontrollers.", createdBy = charlie),
            Event(club = robotics, name = "Regional Robotics Challenge", date = LocalDate.of(2026, 5, 20), location = "Sports Hall", eventType = competition, description = "Compete against other university teams.", createdBy = charlie),
            Event(club = robotics, name = "AI in Robotics Seminar", date = LocalDate.of(2026, 4, 18), location = "Room B202", eventType = talk, description = "How machine learning is changing robotics.", createdBy = charlie),
            Event(club = robotics, name = "Weekly Build Meeting", date = LocalDate.of(2026, 3, 25), location = "Lab 3", eventType = meeting, description = "Regular weekly session to work on ongoing projects.", createdBy = charlie),
            Event(club = photography, name = "Portrait Photography Workshop", date = LocalDate.of(2026, 3, 22), location = "Studio 1", eventType = workshop, description = "Lighting and composition for portraits.", createdBy = charlie),
            Event(club = photography, name = "Spring Photo Walk", date = LocalDate.of(2026, 4, 10), location = "Campus Gardens", eventType = social, description = "Casual outdoor shoot around campus.", createdBy = charlie),
            Event(club = photography, name = "Annual Photo Exhibition", date = LocalDate.of(2026, 6, 1), location = "Gallery Hall", eventType = other, description = "Members showcase their best work of the year.", createdBy = charlie),
            Event(club = hiking, name = "Serra da Arrábida Hike", date = LocalDate.of(2026, 3, 29), location = "Arrábida Natural Park", eventType = social, description = "Full-day hike with stunning coastal views.", createdBy = charlie),
            Event(club = hiking, name = "Navigation & Orienteering Workshop", date = LocalDate.of(2026, 4, 25), location = "Campus Field", eventType = workshop, description = "Learn to use map and compass in the field.", createdBy = charlie),
            Event(club = hiking, name = "End of Season BBQ", date = LocalDate.of(2026, 6, 15), location = "Campus Park", eventType = social, description = "Celebrate the hiking season with food and friends.", createdBy = charlie),
            Event(club = film, name = "Kubrick Retrospective", date = LocalDate.of(2026, 3, 18), location = "Lecture Hall C", eventType = other, description = "Screening and discussion of 2001: A Space Odyssey.", createdBy = charlie),
            Event(club = film, name = "Short Film Competition", date = LocalDate.of(2026, 5, 3), location = "Auditorium", eventType = competition, description = "Submit your short film and win prizes.", createdBy = charlie),
            Event(club = film, name = "Screenwriting Workshop", date = LocalDate.of(2026, 4, 14), location = "Room D305", eventType = workshop, description = "From idea to script in one afternoon.", createdBy = charlie)
        ))
    }
}