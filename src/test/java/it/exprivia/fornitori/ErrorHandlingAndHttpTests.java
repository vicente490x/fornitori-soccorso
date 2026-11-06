package it.exprivia.fornitori;

import static org.assertj.core.api.Assertions.assertThat;

import it.exprivia.fornitori.service.MapProviders;
import it.exprivia.fornitori.service.RegistryService;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/** Punto 18 e ciò che si vede solo su HTTP reale: cookie, intestazioni, limiti di dimensione, errori. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "fornitori.missions.expiry-delay-ms=3600000")
@EnabledIfEnvironmentVariable(named = "FORNITORI_TEST_DATABASE", matches = "isolated")
class ErrorHandlingAndHttpTests {
    // La stessa delle altre classi: una password diversa bloccherebbe il login dei test eseguiti dopo.
    private static final String PASSWORD = IsolatedPostgresTest.PASSWORD;

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @MockitoSpyBean RegistryService registry;
    @MockitoBean MapProviders.GeocodingClient geocoding;
    @MockitoBean MapProviders.RoutingClient routing;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder url(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
    }

    private String cookie(HttpResponse<String> response) {
        return response.headers().firstValue("Set-Cookie").orElse("").split(";")[0];
    }

    private String[] loginManager() throws Exception {
        assertThat(jdbc.queryForObject("select current_database()", String.class)).startsWith("fornitori_e2e");
        jdbc.update("update app_users set password_hash = ? where email = 'responsabile@demo.it'", encoder.encode(PASSWORD));
        var csrf = send(url("/api/v1/auth/csrf"));
        String sessionCookie = cookie(csrf);
        String token = csrf.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        var login = send(url("/api/v1/auth/login").header("Cookie", sessionCookie).header("X-CSRF-TOKEN", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"email\":\"responsabile@demo.it\",\"password\":\"" + PASSWORD + "\"}")));
        assertThat(login.statusCode()).isEqualTo(200);
        String active = login.headers().firstValue("Set-Cookie").map(value -> value.split(";")[0]).orElse(sessionCookie);
        var fresh = send(url("/api/v1/auth/csrf").header("Cookie", active));
        return new String[] {active, fresh.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1")};
    }

    @Test
    void sessionCookieIsHttpOnlyAndSameSite() throws Exception {
        String header = send(url("/api/v1/auth/csrf")).headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(header).contains("HttpOnly", "SameSite=Lax");
    }

    @Test
    void securityHeadersAreSentOnPagesAndApis() throws Exception {
        for (String path : List.of("/", "/api/v1/auth/csrf")) {
            var headers = send(url(path)).headers();
            assertThat(headers.firstValue("X-Content-Type-Options")).contains("nosniff");
            assertThat(headers.firstValue("X-Frame-Options")).contains("DENY");
            assertThat(headers.firstValue("Content-Security-Policy").orElse(""))
                    .contains("default-src 'self'", "frame-ancestors 'none'", "connect-src 'self'");
            assertThat(headers.firstValue("Referrer-Policy")).isPresent();
        }
    }

    @Test
    void rescueAssetsArePubliclyServed() throws Exception {
        for (String path : List.of("/rescue-ui.js", "/rescue-ui.css", "/images/rescue-cat.svg")) {
            var response = send(url(path));
            assertThat(response.statusCode()).as(path).isEqualTo(200);
        }
    }

    @Test
    void oversizedBodyIsRefusedBeforeReachingTheController() throws Exception {
        var response = send(url("/api/v1/auth/login").header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + "x".repeat(70_000) + "\"}")));
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(response.body()).contains("\"status\":413");
    }

    @Test
    void chunkedBodyWithoutDeclaredLengthIsRefused() throws Exception {
        var response = send(url("/api/v1/auth/login").header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(new byte[100]))));
        assertThat(response.statusCode()).isEqualTo(411);
    }

    @Test
    void unknownApiPathGivesNotFoundToAnAuthorizedUser() throws Exception {
        String[] manager = loginManager();
        var response = send(url("/api/v1/inesistente").header("Cookie", manager[0]));
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).isEqualTo("{\"status\":404,\"error\":\"not_found\"}");
        var designed = send(url("/api/v1/utenti").header("Cookie", manager[0]));
        assertThat(designed.statusCode()).isEqualTo(404);
    }

    @Test
    void unexpectedErrorsGiveAGenericJsonWithoutInternals() throws Exception {
        String[] manager = loginManager();
        Mockito.doThrow(new RuntimeException("segreto-interno SELECT * FROM app_users")).when(registry).companies();
        try {
            var response = send(url("/api/v1/compagnie").header("Cookie", manager[0]));
            assertThat(response.statusCode()).isEqualTo(500);
            assertThat(response.body()).contains("\"error\":\"internal_error\"").doesNotContain("segreto", "SELECT", "Exception", "at it.");
        } finally {
            Mockito.reset(registry);
        }
    }

    @Test
    void malformedRequestsGiveUniformErrors() throws Exception {
        String[] manager = loginManager();
        var notNumeric = send(url("/api/v1/missioni/abc").header("Cookie", manager[0]));
        assertThat(notNumeric.statusCode()).isEqualTo(400);
        assertThat(notNumeric.body()).contains("invalid_request");
        var badJson = send(url("/api/v1/missioni").header("Cookie", manager[0]).header("X-CSRF-TOKEN", manager[1])
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{rotto")));
        assertThat(badJson.statusCode()).isEqualTo(400);
        assertThat(badJson.body()).doesNotContain("Exception");
        var wrongMethod = send(url("/api/v1/missioni").header("Cookie", manager[0]).header("X-CSRF-TOKEN", manager[1])
                .PUT(HttpRequest.BodyPublishers.ofString("{}")));
        assertThat(wrongMethod.statusCode()).isEqualTo(405);
        var wrongType = send(url("/api/v1/missioni").header("Cookie", manager[0]).header("X-CSRF-TOKEN", manager[1])
                .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("x")));
        assertThat(wrongType.statusCode()).isEqualTo(415);
    }
}
