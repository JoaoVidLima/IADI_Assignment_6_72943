package pt.unl.fct.iadi.novaevents.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(@Value("\${jwt.secret}") secret: String) {

    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expirationMs = 3_600_000L // 1 hour

    // Called by JwtAuthSuccessHandler after successful login.
    // Produces a signed JWT with username and roles as claims.
    fun generate(username: String, roles: List<String>): String =
        Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    // Called by JwtCookieAuthFilter on every request.
    // Returns the claims if the token is valid, null if invalid or expired.
    fun validate(token: String): Map<String, Any>? = runCatching {
        val claims: Claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        @Suppress("UNCHECKED_CAST")
        mapOf("name" to claims.subject, "roles" to claims["roles"] as List<String>)
    }.getOrNull()
}