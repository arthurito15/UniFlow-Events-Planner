package fr.mif10.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:uniflow-security-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.data-locations=classpath:test-data.sql",
        "app.security.token-secret=test-secret",
        "app.security.token-ttl-seconds=7200"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProtectAdminEndpointsWithBearerTokenAndRole() throws Exception {
        mockMvc.perform(post("/api/poles/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Securite"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        String userToken = registerAndReadToken();
        mockMvc.perform(post("/api/poles/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Securite"
                                }
                                """))
                .andExpect(status().isForbidden());

        String adminToken = loginAndReadToken("admin@univ-lyon1.fr", "admin123");
        assertJwtFormat(adminToken);
        mockMvc.perform(post("/api/poles/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Securite"
                                }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Securite"));

        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Securite",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/organizers/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Securite",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/organizers/create")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Securite",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Club Securite"));
    }

    @Test
    void shouldAllowOrganizerToDeleteEventWithBearerToken() throws Exception {
        String organizerToken = loginAndReadToken("organisateur@univ-lyon1.fr", "org123");
        Long eventId = findTestEventId();

        mockMvc.perform(delete("/api/events/" + eventId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Événement supprimé avec succès"));

        mockMvc.perform(get("/api/events/" + eventId))
                .andExpect(status().isNotFound());
    }

    private String registerAndReadToken() throws Exception {
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Martin",
                                  "prenom": "Lou",
                                  "email": "lou.martin@univ-lyon1.fr",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readToken(json);
    }

    private String loginAndReadToken(String email, String password) throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return readToken(json);
    }

    private String readToken(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.get("token").asText();
    }

    private Long findTestEventId() throws Exception {
        String json = mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode events = objectMapper.readTree(json);
        for (JsonNode event : events) {
            if ("Evenement test organisateur".equals(event.get("title").asText())) {
                return event.get("id").asLong();
            }
        }
        throw new IllegalStateException("Evenement test organisateur introuvable");
    }

    private void assertJwtFormat(String token) {
        org.assertj.core.api.Assertions.assertThat(token.split("\\.")).hasSize(3);
    }
}
