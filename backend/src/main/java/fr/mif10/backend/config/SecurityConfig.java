package fr.mif10.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import fr.mif10.backend.security.TokenAuthenticationFilter;
import fr.mif10.backend.security.TokenService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenService tokenService;

    public SecurityConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Safe for this stateless REST API: authentication uses explicit Bearer tokens
                // in the Authorization header, with form login, HTTP basic, logout and sessions disabled.
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**", "/api/health", "/h2-console/**").permitAll()
                        .requestMatchers("/api/events/favori/**").authenticated()
                        .requestMatchers("/api/inscriptions/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/poles/**", "/api/organizers/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/poles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/poles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/poles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/organizers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/organizers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ADMIN", "ORGANISATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/users/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new TokenAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
