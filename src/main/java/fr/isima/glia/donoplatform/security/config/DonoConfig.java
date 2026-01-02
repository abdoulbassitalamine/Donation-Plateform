package fr.isima.glia.donoplatform.security.config;

import fr.isima.glia.donoplatform.security.jwt.AuthTokenFilter;
import fr.isima.glia.donoplatform.security.jwt.JwtAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class DonoConfig {

    private final JwtAuthEntryPoint authEntryPoint;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ============================================================
    // CHAÎNE 1 : Requêtes JSON (Accept: application/json) → JWT
    // ============================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new RequestHeaderRequestMatcher("Accept", "application/json"))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth publique (même URL que HTML, mais JSON)
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        // Ressources publiques
                        .requestMatchers("/", "/announcements", "/announcements/*").permitAll()
                        .requestMatchers("/images/*/download").permitAll()
                        .requestMatchers("/search", "/search/results").permitAll()
                        // Tout le reste nécessite JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // CHAÎNE 2 : Requêtes HTML (Accept: text/html) → Sessions
    // ============================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        // Pages publiques
                        .requestMatchers("/", "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/announcements").permitAll()                    // Liste
                        .requestMatchers("/announcements/{id:\\d+}").permitAll()          // Détail (ID numérique)
                        .requestMatchers("/search", "/search/results").permitAll()
                        .requestMatchers("/images/*/download").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/h2-console/**").permitAll()

                        // Pages privées - AUTHENTIFICATION REQUISE
                        .requestMatchers("/announcements/create").authenticated()         // Créer annonce
                        .requestMatchers("/announcements/{id}/edit").authenticated()      // Éditer annonce
                        .requestMatchers("/favorites/**").authenticated()                 // Favoris
                        .requestMatchers("/batches/**").authenticated()                   // Lots
                        .requestMatchers("/messages/**").authenticated()                  // Messages
                        .requestMatchers("/users/**").authenticated()                     // Profil
                        .requestMatchers("/search/saved/**").authenticated()              // Recherches sauvegardées

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}