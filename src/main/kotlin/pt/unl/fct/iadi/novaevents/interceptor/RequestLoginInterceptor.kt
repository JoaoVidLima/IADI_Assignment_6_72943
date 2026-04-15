package pt.unl.fct.iadi.novaevents.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestLoggingInterceptor : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    // afterCompletion runs after the response is finalised — status code is available here
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = if (authentication != null && authentication.isAuthenticated
            && authentication.name != "anonymousUser")
            authentication.name
        else
            "anonymous"

        log.info("[{}] {} {} [{}]", principal, request.method, request.requestURI, response.status)
    }
}