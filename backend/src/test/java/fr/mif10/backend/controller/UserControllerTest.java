package fr.mif10.backend.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import fr.mif10.backend.repository.OrganisationRepository;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompteRepository compteRepository;

    @MockBean
    private OrganisationRepository organisationRepository;

    @Test
    void shouldReturnAllUsersForAdminView() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        user.setId(1L);
        user.setDateInscription(LocalDateTime.of(2026, 3, 24, 12, 0));
        user.addRole(UserRole.ORGANISATEUR);
        user.addOrganisation(new Organisation("Club Info", new Pole("Informatique")));
        user.addOrganisation(new Organisation("BDE", new Pole("Vie etudiante")));
        given(compteRepository.findAllWithOrganisations()).willReturn(List.of(user));

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Dupont"))
                .andExpect(jsonPath("$[0].email").value("alice@univ-lyon1.fr"))
                .andExpect(jsonPath("$[0].roles").value(org.hamcrest.Matchers.hasItem("Organisateur")))
                .andExpect(jsonPath("$[0].organizations").value(org.hamcrest.Matchers.contains("BDE", "Club Info")))
                .andExpect(jsonPath("$[0].organization").value("BDE"))
                .andExpect(jsonPath("$[0].registrationDate").value("2026-03-24"));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        user.setDateInscription(LocalDateTime.of(2026, 3, 24, 12, 0));
        given(compteRepository.findById(1L)).willReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.email").value("alice@univ-lyon1.fr"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        given(compteRepository.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Compte introuvable."));
    }

    @Test
    void shouldAssignOrganizationToUser() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        user.setId(1L);
        user.setDateInscription(LocalDateTime.of(2026, 3, 24, 12, 0));
        Organisation organisation = new Organisation("Club Info", new Pole("Informatique"));

        given(compteRepository.findByIdWithOrganisations(1L)).willReturn(Optional.of(user));
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info"))
                .willReturn(Optional.of(organisation));
        given(compteRepository.save(user)).willReturn(user);

        mockMvc.perform(put("/api/users/1/organization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organization": "Club Info"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("Organisateur")))
                .andExpect(jsonPath("$.organizations").value(org.hamcrest.Matchers.contains("Club Info")))
                .andExpect(jsonPath("$.organization").value("Club Info"));

        verify(organisationRepository).save(organisation);
        verify(compteRepository).save(user);
    }

    @Test
    void shouldAssignMultipleOrganizationsToUser() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        user.setId(1L);
        user.setDateInscription(LocalDateTime.of(2026, 3, 24, 12, 0));
        Organisation clubInfo = new Organisation("Club Info", new Pole("Informatique"));
        Organisation bde = new Organisation("BDE", new Pole("Vie etudiante"));

        given(compteRepository.findByIdWithOrganisations(1L)).willReturn(Optional.of(user));
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info"))
                .willReturn(Optional.of(clubInfo));
        given(organisationRepository.findByNomStructureIgnoreCase("BDE"))
                .willReturn(Optional.of(bde));
        given(compteRepository.save(user)).willReturn(user);

        mockMvc.perform(put("/api/users/1/organization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizations": ["Club Info", "BDE"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("Organisateur")))
                .andExpect(jsonPath("$.organization").value("BDE"))
                .andExpect(jsonPath("$.organizations").value(org.hamcrest.Matchers.contains("BDE", "Club Info")));

        verify(organisationRepository).save(clubInfo);
        verify(organisationRepository).save(bde);
        verify(compteRepository).save(user);
    }

    @Test
    void shouldRemoveOrganizationFromUser() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        Organisation organisation = new Organisation("Club Info", new Pole("Informatique"));
        user.addRole(UserRole.ORGANISATEUR);
        user.addOrganisation(organisation);
        organisation.addMembre(user);

        given(compteRepository.findByIdWithOrganisations(1L)).willReturn(Optional.of(user));
        given(compteRepository.save(user)).willReturn(user);

        mockMvc.perform(put("/api/users/1/organization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organization": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("Organisateur"))))
                .andExpect(jsonPath("$.organizations").isEmpty());

        verify(organisationRepository).save(organisation);
        verify(compteRepository).save(user);
    }

    @Test
    void shouldRemoveAllOrganizationsWithEmptyOrganizationList() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        Organisation clubInfo = new Organisation("Club Info", new Pole("Informatique"));
        Organisation bde = new Organisation("BDE", new Pole("Vie etudiante"));
        user.addRole(UserRole.ORGANISATEUR);
        user.addOrganisation(clubInfo);
        user.addOrganisation(bde);
        clubInfo.addMembre(user);
        bde.addMembre(user);

        given(compteRepository.findByIdWithOrganisations(1L)).willReturn(Optional.of(user));
        given(compteRepository.save(user)).willReturn(user);

        mockMvc.perform(put("/api/users/1/organization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizations": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("Organisateur"))))
                .andExpect(jsonPath("$.organizations").isEmpty());

        verify(organisationRepository).save(clubInfo);
        verify(organisationRepository).save(bde);
        verify(compteRepository).save(user);
    }

    @Test
    void shouldReturnNotFoundAndNotMutateWhenOrganizationDoesNotExist() throws Exception {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        Organisation existingOrganisation = new Organisation("Club Info", new Pole("Informatique"));
        user.addRole(UserRole.ORGANISATEUR);
        user.addOrganisation(existingOrganisation);

        given(compteRepository.findByIdWithOrganisations(1L)).willReturn(Optional.of(user));
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info"))
                .willReturn(Optional.of(existingOrganisation));
        given(organisationRepository.findByNomStructureIgnoreCase("Orga inconnue"))
                .willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1/organization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "organizations": ["Club Info", "Orga inconnue"]
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(organisationRepository, never()).save(existingOrganisation);
        verify(compteRepository, never()).save(user);
    }

    @Test
    void shouldDeleteExistingUser() throws Exception {
        given(compteRepository.existsById(1L)).willReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilisateur supprimé avec succès"));

        verify(compteRepository).deleteFavoritesByCompteId(1L);
        verify(compteRepository).deleteInscriptionsByCompteId(1L);
        verify(compteRepository).deleteOrganisationMembershipsByCompteId(1L);
        verify(compteRepository).deleteRolesByCompteId(1L);
        verify(compteRepository).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownUser() throws Exception {
        given(compteRepository.existsById(404L)).willReturn(false);

        mockMvc.perform(delete("/api/users/404"))
                .andExpect(status().isNotFound());

        verify(compteRepository, never()).deleteById(404L);
    }
}
