package it.exprivia.fornitori.config;

import it.exprivia.fornitori.enums.UserRole;
import it.exprivia.fornitori.service.DatabaseUserDetailsService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    /** Bootstrap e Leaflet arrivano dalle CDN, i tile dalla mappa; gli stili inline servono a Leaflet e Bootstrap. */
    private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; "
            + "script-src 'self' https://cdn.jsdelivr.net https://unpkg.com; "
            + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com; "
            + "img-src 'self' data: https://tile.openstreetmap.org https://unpkg.com; "
            + "font-src 'self' data: https://cdn.jsdelivr.net; "
            + "connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(
            DatabaseUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    @Order(1)
    SecurityFilterChain chatSecurityFilterChain(
            HttpSecurity http,
            @Value("${fornitori.chat.service-token:}") String chatServiceToken) throws Exception {
        http
                .securityMatcher("/api/v1/chat/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ChatServiceTokenFilter(chatServiceToken), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole(ChatServiceTokenFilter.ROLE))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, 401, "unauthorized"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, 403, "forbidden")))
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .headers(headers -> headers
                        .contentSecurityPolicy(policy -> policy.policyDirectives(CONTENT_SECURITY_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy", "geolocation=(), camera=(), microphone=()")))
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                        .requireExplicitSave(true))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository)
                        .requireCsrfProtectionMatcher(request ->
                                !java.util.Set.of("GET", "HEAD", "TRACE", "OPTIONS").contains(request.getMethod())
                                || (java.util.Set.of("GET", "HEAD").contains(request.getMethod()) && request.getRequestURI()
                                    .equals(request.getContextPath() + "/api/v1/maps/route"))))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/demo.html", "/demo.js", "/demo.css", "/demo-banner.js", "/api/v1/demo/**").permitAll()
                        .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/public-site.css", "/favicon.ico", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/maps/route")
                        .hasAnyRole(UserRole.AUTISTA.name(), UserRole.RESPONSABILE.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        .requestMatchers("/api/v1/autista/**").hasRole(UserRole.AUTISTA.name())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/missioni/*/accept",
                                "/api/v1/missioni/*/reject",
                                "/api/v1/missioni/*/complete")
                        .hasRole(UserRole.AUTISTA.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/missioni/*", "/api/v1/missioni/*/notes")
                        .hasAnyRole(UserRole.AUTISTA.name(), UserRole.RESPONSABILE.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/missioni/*/notes", "/api/v1/missioni/*/annulla")
                        .hasAnyRole(UserRole.AUTISTA.name(), UserRole.RESPONSABILE.name())
                        .requestMatchers("/api/v1/**").hasRole(UserRole.RESPONSABILE.name())
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, 401, "unauthorized"))
                        .accessDeniedHandler((request, response, exception) -> {
                            logger.warn("Accesso negato: {} {}", request.getMethod(), request.getRequestURI());
                            writeError(response, 403, "forbidden");
                        }))
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }

    private static void writeError(HttpServletResponse response, int status, String error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\"}");
    }
}
