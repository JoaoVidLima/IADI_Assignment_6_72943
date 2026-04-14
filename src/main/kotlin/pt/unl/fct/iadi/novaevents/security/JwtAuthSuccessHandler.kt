package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.stereotype.Component

@Component
class JwtAuthSuccessHandler(
    private val jwtService: JwtService
) : AuthenticationSuccessHandler {

    private val requestCache = CookieRequestCache()

    // Called by Spring after UsernamePasswordAuthenticationFilter succeeds.
    // Generates a JWT, writes it as an HttpOnly cookie, then redirects.
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val token = jwtService.generate(
            username = authentication.name,
            roles = authentication.authorities.map { it.authority }
        )

        val cookie = Cookie("jwt", token).apply {
            isHttpOnly = true  // not accessible via JS — protection against XSS
            path = "/"
            maxAge = 3600
        }
        response.addCookie(cookie)

        // Redirect to the page the user was trying to reach before being sent to /login,
        // or fall back to homepage if they went to /login directly.
        val savedRequest = requestCache.getRequest(request, response)
        val redirectUrl = savedRequest?.redirectUrl ?: (request.contextPath + "/")
        requestCache.removeRequest(request, response)
        response.sendRedirect(redirectUrl)
    }
}