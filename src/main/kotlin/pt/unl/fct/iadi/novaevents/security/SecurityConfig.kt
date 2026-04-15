package pt.unl.fct.iadi.novaevents.security

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

                    // Add /error and /favicon.ico here!
                    .requestMatchers(HttpMethod.GET, "/login", "/logout", "/", "/error", "/favicon.ico").permitAll()

                    // Static resources (Bootstrap, etc) if you have them in /static
                    .requestMatchers("/css/**", "/js/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/clubs/**").permitAll()

                    // Public: all reads
                    .requestMatchers(HttpMethod.GET, "/login", "/logout").permitAll()
                    .requestMatchers(HttpMethod.GET, "/clubs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/").permitAll()

                    // EDITOR or ADMIN: create and edit events
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/new").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")

                    // ADMIN only: delete
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*/delete").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events/*/delete").hasRole("ADMIN")

                    // Everything else: just be logged in
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