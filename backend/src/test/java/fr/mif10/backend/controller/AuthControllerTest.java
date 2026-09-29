package fr.mif10.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.entity.UserRole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.security.TokenService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompteRepository compteRepository;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setupTokenService() {
        given(tokenService.generateToken(any(Compte.class))).willReturn("test.jwt.token");
    }

    @Test
    void shouldRegisterUserWithUniversityEmail() throws Exception {
        Compte savedUser = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        given(compteRepository.findByEmail("alice@univ-lyon1.fr")).willReturn(Optional.empty());
        given(compteRepository.save(any(Compte.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Dupont",
                                  "prenom": "Alice",
                                  "email": "alice@univ-lyon1.fr",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").value(
                        org.hamcrest.Matchers.hasItem("UTILISATEUR")
                ))
                .andExpect(jsonPath("$.displayName").value("Alice Dupont"))
                .andExpect(jsonPath("$.email").value("alice@univ-lyon1.fr"))
                .andExpect(jsonPath("$.message").value("Inscription réussie."))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));

        verify(compteRepository).save(any(Compte.class));
    }

    @Test
    void shouldRejectInvalidLogin() throws Exception {
        given(compteRepository.findByEmail("alice@univ-lyon1.fr")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@univ-lyon1.fr",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides."));
    }

    @Test
    void shouldRejectRegistrationWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "",
                                  "prenom": "Alice",
                                  "email": "alice@univ-lyon1.fr",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tous les champs sont obligatoires."));

        verify(compteRepository, never()).save(any(Compte.class));
    }

    @Test
    void shouldRejectRegistrationOutsideUniversityDomain() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Dupont",
                                  "prenom": "Alice",
                                  "email": "alice@example.org",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'adresse email doit appartenir au domaine universitaire."));

        verify(compteRepository, never()).save(any(Compte.class));
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() throws Exception {
        Compte existingUser = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        given(compteRepository.findByEmail("alice@univ-lyon1.fr")).willReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Dupont",
                                  "prenom": "Alice",
                                  "email": "alice@univ-lyon1.fr",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Un compte existe deja avec cet email."));

        verify(compteRepository, never()).save(any(Compte.class));
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        given(compteRepository.findByEmail("alice@univ-lyon1.fr")).willReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@univ-lyon1.fr",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides."));
    }

    @Test
    void shouldReturnAdministratorDisplayNameWhenNamesAreMissing() throws Exception {
        Compte admin = new Compte("admin@univ-lyon1.fr", "admin123", null, null);
        admin.addRole(UserRole.ADMIN);
        given(compteRepository.findByEmail("admin@univ-lyon1.fr")).willReturn(Optional.of(admin));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@univ-lyon1.fr",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Administrateur"))
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("ADMIN")))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void shouldLoginOrganizerWithSharedEndpoint() throws Exception {

        Compte user = new Compte("test@univ-lyon1.fr", "secret123", "Nom", "Prénom");
        user.addRole(UserRole.ORGANISATEUR);

        Organisation orga = new Organisation("BDE Info", new Pole("Asso"));
        user.addOrganisation(orga);
        orga.addMembre(user);

        given(compteRepository.findByEmail("test@univ-lyon1.fr"))
                .willReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "test@univ-lyon1.fr",
                          "password": "secret123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").value(
                        org.hamcrest.Matchers.hasItem("ORGANISATEUR")
                ))
                .andExpect(jsonPath("$.organisations[0]").value("BDE Info"))
                .andExpect(jsonPath("$.displayName").value("Prénom Nom"))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void shouldLoginSimpleUserWithoutOrganisations() throws Exception {

        Compte user = new Compte("user@univ-lyon1.fr", "secret123", "Nom", "Prénom");

        given(compteRepository.findByEmail("user@univ-lyon1.fr"))
                .willReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@univ-lyon1.fr",
                          "password": "secret123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisations").isEmpty())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }
}
