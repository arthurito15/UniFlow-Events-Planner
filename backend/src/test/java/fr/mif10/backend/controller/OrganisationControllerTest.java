package fr.mif10.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import fr.mif10.backend.repository.PoleRepository;

@WebMvcTest(OrganisationController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganisationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganisationRepository organisationRepository;

    @MockBean
    private PoleRepository poleRepository;

    @MockBean
    private CompteRepository compteRepository;

    @Test
    void shouldReturnAnEmptyListWhenNoOrganisationExists() throws Exception {
        given(organisationRepository.findAllWithPoleAndMembers()).willReturn(List.of());

        mockMvc.perform(get("/api/organizers/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnOrganisationsAsDto() throws Exception {
        Organisation organisation = organisation("Club Info");
        organisation.addMembre(new Compte("club.info@univ-lyon1.fr", "secret", "Info", "Club"));
        given(organisationRepository.findAllWithPoleAndMembers()).willReturn(List.of(organisation));

        mockMvc.perform(get("/api/organizers/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].name").value("Club Info"))
                .andExpect(jsonPath("$[0].nomStructure").value("Club Info"))
                .andExpect(jsonPath("$[0].email").value("club.info@univ-lyon1.fr"))
                .andExpect(jsonPath("$[0].pole").value("Informatique"))
                .andExpect(jsonPath("$[0].status").value("Actif"));
    }

    @Test
    void shouldReturnOrganisationById() throws Exception {
        given(organisationRepository.findByIdWithPoleAndMembers(12L)).willReturn(Optional.of(organisation("Club Info")));

        mockMvc.perform(get("/api/organizers/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Club Info"));
    }

    @Test
    void shouldReturnNotFoundWhenOrganisationDoesNotExist() throws Exception {
        given(organisationRepository.findByIdWithPoleAndMembers(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/organizers/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateOrganisationWithExistingMember() throws Exception {
        Pole pole = new Pole("Informatique");
        Compte member = new Compte("club.info@univ-lyon1.fr", "secret", "Info", "Club");
        Organisation saved = organisation("Club Info");

        given(organisationRepository.findByNomStructureIgnoreCase("Club Info")).willReturn(Optional.empty());
        given(poleRepository.findByName("Informatique")).willReturn(Optional.of(pole));
        given(compteRepository.findByEmail("club.info@univ-lyon1.fr")).willReturn(Optional.of(member));
        given(organisationRepository.save(any(Organisation.class))).willReturn(saved);
        given(organisationRepository.findByIdWithPoleAndMembers(12L)).willReturn(Optional.of(saved));

        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Info",
                                  "email": "club.info@univ-lyon1.fr",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Club Info"));

        ArgumentCaptor<Organisation> captor = ArgumentCaptor.forClass(Organisation.class);
        verify(organisationRepository).save(captor.capture());
        assertOrganisationCreated(captor.getValue(), member);
        verify(compteRepository).save(member);
    }

    @Test
    void shouldCreateOrganisationFromNomStructureAndPoleId() throws Exception {
        Pole pole = new Pole("Informatique");
        Organisation saved = organisation("BDE");

        given(organisationRepository.findByNomStructureIgnoreCase("BDE")).willReturn(Optional.empty());
        given(poleRepository.findById(3L)).willReturn(Optional.of(pole));
        given(organisationRepository.save(any(Organisation.class))).willReturn(saved);
        given(organisationRepository.findByIdWithPoleAndMembers(12L)).willReturn(Optional.empty());

        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomStructure": "BDE",
                                  "poleId": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("BDE"));
    }

    @Test
    void shouldRejectCreateWhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(organisationRepository, never()).save(any(Organisation.class));
    }

    @Test
    void shouldRejectCreateWhenOrganisationAlreadyExists() throws Exception {
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info"))
                .willReturn(Optional.of(organisation("Club Info")));

        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Info",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isConflict());

        verify(organisationRepository, never()).save(any(Organisation.class));
    }

    @Test
    void shouldRejectCreateWhenPoleDoesNotExist() throws Exception {
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info")).willReturn(Optional.empty());
        given(poleRepository.findByName("Informatique")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/organizers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Info",
                                  "pole": "Informatique"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(organisationRepository, never()).save(any(Organisation.class));
    }

    @Test
    void shouldUpdateOrganisationPole() throws Exception {
        Organisation organisation = organisation("Club Info");
        Pole newPole = new Pole("Culture");

        given(organisationRepository.findByIdWithPoleAndMembers(12L)).willReturn(Optional.of(organisation));
        given(organisationRepository.findByNomStructureIgnoreCase("Club Info"))
                .willReturn(Optional.of(organisation));
        given(poleRepository.findByName("Culture")).willReturn(Optional.of(newPole));
        given(organisationRepository.save(organisation)).willReturn(organisation);

        mockMvc.perform(put("/api/organizers/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Club Info",
                                  "pole": "Culture"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pole").value("Culture"));

        org.assertj.core.api.Assertions.assertThat(organisation.getPole()).isEqualTo(newPole);
        verify(organisationRepository).save(organisation);
    }

    @Test
    void shouldDeleteExistingOrganisation() throws Exception {
        given(organisationRepository.existsById(12L)).willReturn(true);

        mockMvc.perform(delete("/api/organizers/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Organisation supprimée avec succès"));

        verify(organisationRepository).deleteFavoritesByOrganisationId(12L);
        verify(organisationRepository).deleteInscriptionsByOrganisationId(12L);
        verify(organisationRepository).deleteById(12L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownOrganisation() throws Exception {
        given(organisationRepository.existsById(404L)).willReturn(false);

        mockMvc.perform(delete("/api/organizers/404"))
                .andExpect(status().isNotFound());

        verify(organisationRepository, never()).deleteById(404L);
    }

    private static Organisation organisation(String name) {
        Organisation organisation = new Organisation(name, new Pole("Informatique"));
        organisation.setId(12L);
        return organisation;
    }

    private static void assertOrganisationCreated(Organisation organisation, Compte member) {
        org.assertj.core.api.Assertions.assertThat(organisation.getNomStructure()).isEqualTo("Club Info");
        org.assertj.core.api.Assertions.assertThat(organisation.getPole().getName()).isEqualTo("Informatique");
        org.assertj.core.api.Assertions.assertThat(organisation.getMembres()).containsExactly(member);
        org.assertj.core.api.Assertions.assertThat(member.getRoles()).contains(UserRole.ORGANISATEUR);
    }
}
