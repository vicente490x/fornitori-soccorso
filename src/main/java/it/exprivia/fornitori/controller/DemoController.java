package it.exprivia.fornitori.controller;

import it.exprivia.fornitori.dto.AuthDtos;
import it.exprivia.fornitori.service.AuthService;
import it.exprivia.fornitori.service.DatabaseUserDetailsService;
import it.exprivia.fornitori.service.RequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Public entry for fixed synthetic accounts. Absent unless the demo profile is active. */
@Profile("demo")
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {
    private static final Map<String, String> ACCOUNTS = Map.of(
            "centrale", "responsabile@demo.it", "autista", "autista1@demo.it");
    private final DatabaseUserDetailsService users;
    private final AuthService auth;
    private final SecurityContextRepository contexts;
    private final CsrfTokenRepository csrf;
    private final RequestRateLimiter limiter;

    public DemoController(DatabaseUserDetailsService users, AuthService auth,
            SecurityContextRepository contexts, CsrfTokenRepository csrf, RequestRateLimiter limiter,
            @Value("${spring.datasource.url}") String databaseUrl) {
        // A misconfigured demo must never expose accounts from a normal database.
        String database = URI.create(databaseUrl.substring("jdbc:".length())).getPath();
        if (!"/fornitori_demo".equals(database)) {
            throw new IllegalStateException("Il profilo demo richiede il database isolato fornitori_demo");
        }
        this.users = users;
        this.auth = auth;
        this.contexts = contexts;
        this.csrf = csrf;
        this.limiter = limiter;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("demo", true, "syntheticData", true, "profiles", ACCOUNTS.keySet()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.CurrentUser> login(@RequestBody Map<String, String> body,
            HttpServletRequest request, HttpServletResponse response) {
        limiter.check("demo-login:" + request.getRemoteAddr(), 30);
        String profile = body.get("profilo");
        String email = profile == null ? null : ACCOUNTS.get(profile);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profilo demo non valido");
        }
        var user = users.loadUserByUsername(email);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        if (request.getSession(false) != null) request.changeSessionId();
        csrf.saveToken(null, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(auth.currentUser(email));
    }
}
