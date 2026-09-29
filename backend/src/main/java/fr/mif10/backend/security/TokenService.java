package fr.mif10.backend.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.mif10.backend.entity.Compte;

@Service
public class TokenService {

    private static final String ISSUER = "uniflow";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long ttlSeconds;

    public TokenService(
            @Value("${app.security.token-secret}") String secret,
            @Value("${app.security.token-ttl-seconds}") long ttlSeconds
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(Compte compte) {
        List<String> roles = compte.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        Instant now = Instant.now();

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(compte.getEmail())
                .withClaim("id", compte.getId())
                .withClaim("roles", roles)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

    public Optional<AuthenticatedUser> parseToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return Optional.empty();
            }

            DecodedJWT jwt = verifier.verify(token);
            List<String> roles = jwt.getClaim("roles").asList(String.class);
            return Optional.of(new AuthenticatedUser(
                    jwt.getClaim("id").asLong(),
                    jwt.getSubject(),
                    roles != null ? roles : List.of()
            ));
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}
