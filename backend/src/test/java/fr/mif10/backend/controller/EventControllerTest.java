package fr.mif10.backend.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Event;
import fr.mif10.backend.entity.EventStatus;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.EventRepository;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private CompteRepository compteRepository;

    @Test
    void shouldReturnAnEmptyListWhenNoEventExists() throws Exception {
        given(eventRepository.findAllWithOrganisationAndPole()).willReturn(List.of());

        mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnEventsAsDTO() throws Exception {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE Info", pole);
        organisation.setId(12L);
        Event event = new Event(
                "Soiree integration",
                "Accueil des nouveaux etudiants",
                "Campus LyonTech",
                LocalDateTime.of(2026, 9, 15, 18, 0),
                LocalDateTime.of(2026, 9, 15, 23, 0),
                120,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
        given(eventRepository.findAllWithOrganisationAndPole()).willReturn(List.of(event));

        mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Soiree integration"))
                .andExpect(jsonPath("$[0].organisationId").value(12))
                .andExpect(jsonPath("$[0].organisationName").value("BDE Info"))
                .andExpect(jsonPath("$[0].poleName").value("Informatique"));
    }

    @Test
    void shouldReturnEventByIdAsDTO() throws Exception {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE Info", pole);
        organisation.setId(12L);

        Event event = new Event(
                "Forum associations",
                "Presentation des associations",
                "Atrium",
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 16, 0),
                300,
                EventStatus.CLOSED,
                0.0,
                organisation,
                null
        );
        given(eventRepository.findByIdWithOrganisationAndPole(1L)).willReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Forum associations"))
                .andExpect(jsonPath("$.organisationId").value(12))
                .andExpect(jsonPath("$.organisationName").value("BDE Info"));
    }

    @Test
    void shouldReturnEventsByPoleAsDTO() throws Exception {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE", pole);
        Event event = new Event(
                "Atelier Git",
                "Initiation aux workflows Git",
                "Salle TP 1",
                LocalDateTime.of(2026, 11, 4, 14, 0),
                LocalDateTime.of(2026, 11, 4, 16, 0),
                30,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
        given(eventRepository.findByPoleName("Informatique")).willReturn(List.of(event));

        mockMvc.perform(get("/api/events").param("pole", "Informatique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Atelier Git"))
                .andExpect(jsonPath("$[0].poleName").value("Informatique"));
    }

    @Test
    void shouldReturnEventsByOrganisationAsDTO() throws Exception {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("Club Info", pole);
        organisation.setId(12L);
        Event event = new Event(
                "Atelier Docker",
                "Initiation conteneurs",
                "Salle TP 2",
                LocalDateTime.of(2026, 11, 6, 14, 0),
                LocalDateTime.of(2026, 11, 6, 16, 0),
                30,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
        given(eventRepository.findByOrganisationIdWithOrganisationAndPole(12L)).willReturn(List.of(event));

        mockMvc.perform(get("/api/events/orga/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Atelier Docker"))
                .andExpect(jsonPath("$[0].organisationName").value("Club Info"));
    }

    @Test
    void shouldReturnFavoriteEvents() throws Exception {
        Compte compte = new Compte("lou.martin@univ-lyon1.fr", "hash", "Martin", "Lou");
        compte.addFavori(event("Favori", LocalDateTime.of(2026, 6, 10, 18, 0)));
        given(compteRepository.findByIdWithEventFavoris(1L)).willReturn(Optional.of(compte));

        mockMvc.perform(get("/api/events/favori/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Favori"));
    }

    @Test
    void shouldAddFavoriteEvent() throws Exception {
        Compte compte = new Compte("lou.martin@univ-lyon1.fr", "hash", "Martin", "Lou");
        Event event = event("Forum", LocalDateTime.of(2026, 6, 10, 18, 0));
        given(compteRepository.findByIdWithEventFavoris(1L)).willReturn(Optional.of(compte));
        given(eventRepository.findById(2L)).willReturn(Optional.of(event));

        mockMvc.perform(post("/api/events/favori/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Favori ajouté avec succès"));

        verify(compteRepository).save(compte);
    }

    @Test
    void shouldRemoveFavoriteEvent() throws Exception {
        Compte compte = new Compte("lou.martin@univ-lyon1.fr", "hash", "Martin", "Lou");
        Event event = event("Forum", LocalDateTime.of(2026, 6, 10, 18, 0));
        compte.addFavori(event);
        given(compteRepository.findByIdWithEventFavoris(1L)).willReturn(Optional.of(compte));
        given(eventRepository.findById(2L)).willReturn(Optional.of(event));

        mockMvc.perform(delete("/api/events/favori/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Favori supprimé avec succès"));

        verify(compteRepository).save(compte);
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        given(eventRepository.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/events/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEventsSortedByBeginDateWithNullDatesLast() throws Exception {
        Event noDate = event("Sans date", null);
        Event oldEvent = event("Ancien", LocalDateTime.of(2026, 1, 10, 9, 0));
        Event recentEvent = event("Recent", LocalDateTime.of(2026, 2, 10, 9, 0));
        given(eventRepository.findAllWithOrganisationAndPole()).willReturn(List.of(noDate, oldEvent, recentEvent));

        mockMvc.perform(get("/api/events/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Recent"))
                .andExpect(jsonPath("$[1].title").value("Ancien"))
                .andExpect(jsonPath("$[2].title").value("Sans date"));
    }

    @Test
    void shouldCreateEventWhenOrganisationIdIsProvided() throws Exception {
        Event savedEvent = event("Conference IA", LocalDateTime.of(2026, 3, 20, 18, 0));
        given(eventRepository.save(any(Event.class))).willReturn(savedEvent);

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Conference IA"));

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldRejectCreateWhenOrganisationIsMissing() throws Exception {
        Event event = event("Conference IA", LocalDateTime.of(2026, 3, 20, 18, 0));
        event.setOrganisation(null);

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldRejectCreateWhenRepositoryFails() throws Exception {
        Event event = event("Conference IA", LocalDateTime.of(2026, 3, 20, 18, 0));
        given(eventRepository.save(any(Event.class))).willThrow(new IllegalStateException("database error"));

        mockMvc.perform(post("/api/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateExistingEvent() throws Exception {
        Event existingEvent = event("Ancien titre", LocalDateTime.of(2026, 4, 1, 10, 0));
        Event eventDetails = event("Nouveau titre", LocalDateTime.of(2026, 5, 1, 10, 0));
        eventDetails.setDescription("Nouvelle description");
        eventDetails.setAddress("Nouvelle salle");
        eventDetails.setCapacity(50);
        eventDetails.setStatus(EventStatus.CANCELED);
        eventDetails.setPrice(12.5);
        given(eventRepository.findById(1L)).willReturn(Optional.of(existingEvent));
        given(eventRepository.save(existingEvent)).willReturn(existingEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nouveau titre"))
                .andExpect(jsonPath("$.description").value("Nouvelle description"))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(eventRepository).save(existingEvent);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownEvent() throws Exception {
        Event eventDetails = event("Nouveau titre", LocalDateTime.of(2026, 5, 1, 10, 0));
        given(eventRepository.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(put("/api/events/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDetails)))
                .andExpect(status().isNotFound());

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldDeleteExistingEvent() throws Exception {
        given(eventRepository.existsById(1L)).willReturn(true);

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Événement supprimé avec succès"));

        verify(eventRepository).deleteFavoritesByEventId(1L);
        verify(eventRepository).deleteInscriptionsByEventId(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownEvent() throws Exception {
        given(eventRepository.existsById(404L)).willReturn(false);

        mockMvc.perform(delete("/api/events/404"))
                .andExpect(status().isNotFound());

        verify(eventRepository, never()).deleteFavoritesByEventId(404L);
        verify(eventRepository, never()).deleteInscriptionsByEventId(404L);
        verify(eventRepository, never()).deleteById(404L);
    }

    private static Event event(String title, LocalDateTime beginDate) {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE Info", pole);
        organisation.setId(12L);
        return new Event(
                title,
                "Description",
                "Campus LyonTech",
                beginDate,
                beginDate != null ? beginDate.plusHours(2) : null,
                120,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
    }
}
