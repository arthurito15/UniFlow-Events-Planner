package fr.mif10.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Event;
import fr.mif10.backend.entity.EventStatus;
import fr.mif10.backend.entity.Inscription;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.EventRepository;
import fr.mif10.backend.repository.InscriptionRepository;

@WebMvcTest(InscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InscriptionRepository inscriptionRepository;

    @MockBean
    private CompteRepository compteRepository;

    @MockBean
    private EventRepository eventRepository;

    @Test
    void shouldReturnAnEmptyListWhenUserHasNoInscription() throws Exception {
        given(inscriptionRepository.findByUserIdWithEventDetails(1L)).willReturn(List.of());

        mockMvc.perform(get("/api/inscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnUserInscriptionsAsEvents() throws Exception {
        given(inscriptionRepository.findByUserIdWithEventDetails(1L))
                .willReturn(List.of(new Inscription(user(), event())));

        mockMvc.perform(get("/api/inscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Forum"))
                .andExpect(jsonPath("$[0].organisationName").value("Club Info"))
                .andExpect(jsonPath("$[0].poleName").value("Informatique"));
    }

    @Test
    void shouldReturnParticipantsForEvent() throws Exception {
        given(eventRepository.existsById(2L)).willReturn(true);
        given(inscriptionRepository.findByEventIdWithUser(2L)).willReturn(List.of(new Inscription(user(), event())));

        mockMvc.perform(get("/api/inscriptions/event/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Martin"))
                .andExpect(jsonPath("$[0].prenom").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@univ-lyon1.fr"));
    }

    @Test
    void shouldReturnNotFoundWhenEventParticipantsAreRequestedForUnknownEvent() throws Exception {
        given(eventRepository.existsById(404L)).willReturn(false);

        mockMvc.perform(get("/api/inscriptions/event/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddInscription() throws Exception {
        Compte user = user();
        Event event = event();
        given(inscriptionRepository.findByUserIdAndEventId(1L, 2L)).willReturn(Optional.empty());
        given(compteRepository.findById(1L)).willReturn(Optional.of(user));
        given(eventRepository.findById(2L)).willReturn(Optional.of(event));

        mockMvc.perform(post("/api/inscriptions/1/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Inscription ajoutée avec succès"));

        ArgumentCaptor<Inscription> captor = ArgumentCaptor.forClass(Inscription.class);
        verify(inscriptionRepository).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getUser()).isEqualTo(user);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getEvent()).isEqualTo(event);
    }

    @Test
    void shouldRejectDuplicateInscription() throws Exception {
        given(inscriptionRepository.findByUserIdAndEventId(1L, 2L))
                .willReturn(Optional.of(new Inscription(user(), event())));

        mockMvc.perform(post("/api/inscriptions/1/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Inscription déjà existante"));

        verify(inscriptionRepository, never()).save(any(Inscription.class));
    }

    @Test
    void shouldReturnNotFoundWhenAddingInscriptionForUnknownUserOrEvent() throws Exception {
        given(inscriptionRepository.findByUserIdAndEventId(1L, 2L)).willReturn(Optional.empty());
        given(compteRepository.findById(1L)).willReturn(Optional.empty());

        mockMvc.perform(post("/api/inscriptions/1/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRemoveInscription() throws Exception {
        Inscription inscription = new Inscription(user(), event());
        given(inscriptionRepository.findByUserIdAndEventId(1L, 2L)).willReturn(Optional.of(inscription));

        mockMvc.perform(delete("/api/inscriptions/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription supprimée avec succès"));

        verify(inscriptionRepository).delete(inscription);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingUnknownInscription() throws Exception {
        given(inscriptionRepository.findByUserIdAndEventId(1L, 2L)).willReturn(Optional.empty());

        mockMvc.perform(delete("/api/inscriptions/1/2"))
                .andExpect(status().isNotFound());
    }

    private static Compte user() {
        Compte user = new Compte("alice@univ-lyon1.fr", "secret", "Martin", "Alice");
        user.setId(1L);
        return user;
    }

    private static Event event() {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("Club Info", pole);
        organisation.setId(12L);
        Event event = new Event(
                "Forum",
                "Description",
                "Campus",
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0),
                80,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
        return event;
    }
}
