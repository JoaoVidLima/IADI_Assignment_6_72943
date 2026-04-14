package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtCookieAuthFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    // Runs once per request, before Spring's authorization checks.
    // Reads the jwt cookie → validates it → if valid, populates the SecurityContext.
    // If no cookie or invalid token, just continues the chain — Spring will handle the 401/redirect.
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.cookies
            ?.firstOrNull { it.name == "jwt" }
            ?.value

        if (token != null) {
            val claims = jwtService.validate(token)
            if (claims != null) {
                @Suppress("UNCHECKED_CAST")
                val authorities = (claims["roles"] as List<String>)
                    .map { SimpleGrantedAuthority(it) }

                val auth = UsernamePasswordAuthenticationToken(
                    claims["name"],  // principal — accessible later as principal.name in controllers
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }
}