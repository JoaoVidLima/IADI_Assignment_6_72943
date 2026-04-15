package pt.unl.fct.iadi.novaevents.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.savedrequest.CookieRequestCache
import pt.unl.fct.iadi.novaevents.security.JwtAuthSuccessHandler
import pt.unl.fct.iadi.novaevents.security.JwtCookieAuthFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtCookieAuthFilter: JwtCookieAuthFilter,
    private val jwtAuthSuccessHandler: JwtAuthSuccessHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun requestCache() = CookieRequestCache()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Stateless — no JSESSIONID issued
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // SecurityContext lives only for the duration of the request (not in session)
            .securityContext {
                it.securityContextRepository(RequestAttributeSecurityContextRepository())
            }
            // CSRF via cookie — required because we're stateless but using cookies
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository())
                csrf.csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
            }
            // Save pre-login destination in a cookie (not session) so we can redirect after login
            .requestCache { it.requestCache(requestCache()) }
            // JWT filter runs before Spring's own auth filter
            .addFilterBefore(jwtCookieAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            // URL-level authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // 1. Static Resources (Always permit)
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                    // 2. Authentication Pages
                    .requestMatchers("/login", "/logout").permitAll()

                    // 3. STRICTEST RULES FIRST: Event Deletion
                    // We allow EDITOR or ADMIN here at the URL level because
                    // the Controller's @PreAuthorize will handle the "Owner OR Admin" logic.
                    .requestMatchers("/clubs/*/events/*/delete").hasAnyRole("EDITOR", "ADMIN")

                    // 4. EDITOR/ADMIN: Creating and Editing
                    // URL bouncer ensures guests can't even see the forms.
                    .requestMatchers("/clubs/*/events/new").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers("/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/clubs/*/events/*").hasAnyRole("EDITOR", "ADMIN")

                    // 5. PUBLIC: Read Operations
                    // We place these AFTER the write rules so that /delete or /edit
                    // aren't accidentally caught by the general /clubs/** permitAll.
                    .requestMatchers(HttpMethod.GET, "/", "/clubs", "/clubs/*", "/events").permitAll()
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*").permitAll()

                    // 6. CATCH-ALL
                    .anyRequest().authenticated()
            }
            // Form login — custom page, custom success handler
            .formLogin { form ->
                form.loginPage("/login").permitAll()
                form.successHandler(jwtAuthSuccessHandler)
            }
            // Logout — clear the jwt cookie
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.deleteCookies("jwt")
                logout.logoutSuccessUrl("/clubs")
            }

        return http.build()
    }
}