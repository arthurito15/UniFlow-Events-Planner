package fr.mif10.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mif10.backend.dto.EventDTO;
import fr.mif10.backend.dto.ParticipantDTO;
import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Event;
import fr.mif10.backend.entity.Inscription;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.EventRepository;
import fr.mif10.backend.repository.InscriptionRepository;
import fr.mif10.backend.security.AuthenticatedUser;

/**
 * Endpoints pour gérer les inscriptions aux événements.
 */
@RestController
@RequestMapping("/api/inscriptions")
public class InscriptionController {

    private final InscriptionRepository inscriptionRepository;
    private final CompteRepository compteRepository;
    private final EventRepository eventRepository;

    /**
     * Crée le controller avec ses repositories.
     */
    public InscriptionController(
            InscriptionRepository inscriptionRepository,
            CompteRepository compteRepository,
            EventRepository eventRepository
    ) {
        this.inscriptionRepository = inscriptionRepository;
        this.compteRepository = compteRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Liste les événements auxquels un utilisateur est inscrit.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<EventDTO>> getByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EventDTO> events = inscriptionRepository.findByUserIdWithEventDetails(userId)
                .stream()
                .map(Inscription::getEvent)
                .map(EventDTO::from)
                .toList();
        return ResponseEntity.ok(events);
    }

    /**
     * Liste les participants d'un événement.
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ParticipantDTO>> getParticipants(@PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.notFound().build();
        }

        List<ParticipantDTO> participants = inscriptionRepository.findByEventIdWithUser(eventId)
                .stream()
                .map(ParticipantDTO::from)
                .toList();
        return ResponseEntity.ok(participants);
    }

    /**
     * Inscrit un utilisateur à un événement.
     */
    @PostMapping("/{userId}/{eventId}")
    public ResponseEntity<Map<String, String>> addInscription(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (inscriptionRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Inscription déjà existante"));
        }

        return compteRepository.findById(userId)
                .flatMap(compte -> eventRepository.findById(eventId)
                        .map(event -> saveInscription(compte, event)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime une inscription.
     */
    @DeleteMapping("/{userId}/{eventId}")
    public ResponseEntity<Map<String, String>> removeInscription(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return inscriptionRepository.findByUserIdAndEventId(userId, eventId)
                .map(inscription -> {
                    inscriptionRepository.delete(inscription);
                    return ResponseEntity.ok(Map.of("message", "Inscription supprimée avec succès"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<Map<String, String>> saveInscription(Compte compte, Event event) {
        inscriptionRepository.save(new Inscription(compte, event));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Inscription ajoutée avec succès"));
    }

    private static boolean isOtherUser(Long userId, AuthenticatedUser authenticatedUser) {
        return authenticatedUser != null && !userId.equals(authenticatedUser.id());
    }
}
