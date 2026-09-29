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

import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.repository.EventRepository;
import fr.mif10.backend.repository.OrganisationRepository;
import fr.mif10.backend.repository.PoleRepository;

@WebMvcTest(PoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class PoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PoleRepository poleRepository;

    @MockBean
    private OrganisationRepository organisationRepository;

    @MockBean
    private EventRepository eventRepository;

    @Test
    void shouldReturnAnEmptyListWhenNoPoleExists() throws Exception {
        given(poleRepository.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/poles/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnPolesAsDto() throws Exception {
        given(poleRepository.findAll()).willReturn(List.of(new Pole("Informatique")));

        mockMvc.perform(get("/api/poles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Informatique"));
    }

    @Test
    void shouldReturnPoleById() throws Exception {
        given(poleRepository.findById(1L)).willReturn(Optional.of(new Pole("Informatique")));

        mockMvc.perform(get("/api/poles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Informatique"));
    }

    @Test
    void shouldReturnNotFoundWhenPoleDoesNotExist() throws Exception {
        given(poleRepository.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/poles/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatePole() throws Exception {
        given(poleRepository.findAll()).willReturn(List.of());
        given(poleRepository.save(any(Pole.class))).willReturn(new Pole("Securite"));

        mockMvc.perform(post("/api/poles/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Securite"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Securite"));

        ArgumentCaptor<Pole> captor = ArgumentCaptor.forClass(Pole.class);
        verify(poleRepository).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getName()).isEqualTo("Securite");
    }

    @Test
    void shouldRejectCreateWhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/poles/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(poleRepository, never()).save(any(Pole.class));
    }

    @Test
    void shouldRejectCreateWhenPoleAlreadyExists() throws Exception {
        given(poleRepository.findAll()).willReturn(List.of(new Pole("Securite")));

        mockMvc.perform(post("/api/poles/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "securite"
                                }
                                """))
                .andExpect(status().isConflict());

        verify(poleRepository, never()).save(any(Pole.class));
    }

    @Test
    void shouldUpdatePole() throws Exception {
        Pole existingPole = new Pole("Ancien");
        given(poleRepository.findById(1L)).willReturn(Optional.of(existingPole));
        given(poleRepository.save(existingPole)).willReturn(existingPole);

        mockMvc.perform(put("/api/poles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nouveau"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nouveau"));

        verify(poleRepository).save(existingPole);
    }

    @Test
    void shouldRejectUpdateWhenNameIsMissing() throws Exception {
        mockMvc.perform(put("/api/poles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(poleRepository, never()).save(any(Pole.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownPole() throws Exception {
        given(poleRepository.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(put("/api/poles/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nouveau"
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(poleRepository, never()).save(any(Pole.class));
    }

    @Test
    void shouldDeleteExistingPole() throws Exception {
        given(poleRepository.existsById(1L)).willReturn(true);
        given(organisationRepository.existsByPoleId(1L)).willReturn(false);
        given(eventRepository.existsByPoleId(1L)).willReturn(false);

        mockMvc.perform(delete("/api/poles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pôle supprimé avec succès"));

        verify(poleRepository).deleteById(1L);
    }

    @Test
    void shouldRejectDeleteWhenPoleIsUsed() throws Exception {
        given(poleRepository.existsById(1L)).willReturn(true);
        given(organisationRepository.existsByPoleId(1L)).willReturn(true);

        mockMvc.perform(delete("/api/poles/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ce pôle est encore utilisé."));

        verify(poleRepository, never()).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownPole() throws Exception {
        given(poleRepository.existsById(404L)).willReturn(false);

        mockMvc.perform(delete("/api/poles/404"))
                .andExpect(status().isNotFound());

        verify(poleRepository, never()).deleteById(404L);
    }
}
