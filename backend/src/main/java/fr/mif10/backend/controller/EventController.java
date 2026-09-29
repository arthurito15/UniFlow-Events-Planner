package fr.mif10.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.mif10.backend.dto.EventDTO;
import fr.mif10.backend.entity.Event;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.EventRepository;
import fr.mif10.backend.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository repo;
    private final CompteRepository compteRepository;

    public EventController(EventRepository repo, CompteRepository compteRepository) {
        this.repo = repo;
        this.compteRepository = compteRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Event> create(@RequestBody Event event) {
        try {
            // Vérifier que l'organisateur existe
            if (event.getOrganisation() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Event savedEvent = repo.save(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<EventDTO>> getAll() {
        List<EventDTO> events = toSortedDtos(repo.findAllWithOrganisationAndPole());
        return ResponseEntity.ok(events);
    }

    @GetMapping(params = "pole")
    public ResponseEntity<List<EventDTO>> getByPole(@RequestParam String pole) {
        List<EventDTO> events = toSortedDtos(repo.findByPoleName(pole));
        return ResponseEntity.ok(events);
    }

    @GetMapping("/favori/{userId}")
    public ResponseEntity<List<EventDTO>> getFavoris(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return compteRepository.findByIdWithEventFavoris(userId)
                .map(compte -> new ArrayList<>(compte.getEventFavoris()))  // Set → List
                .map(EventController::toSortedDtos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/favori/{userId}/{eventId}")
    public ResponseEntity<Map<String, String>> addFavori(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return compteRepository.findByIdWithEventFavoris(userId)
                .flatMap(compte -> repo.findById(eventId)
                        .map(event -> {
                            compte.addFavori(event);
                            compteRepository.save(compte);
                            return ResponseEntity.ok(Map.of("message", "Favori ajouté avec succès"));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/favori/{userId}/{eventId}")
    public ResponseEntity<Map<String, String>> removeFavori(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        if (isOtherUser(userId, authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return compteRepository.findByIdWithEventFavoris(userId)
                .flatMap(compte -> repo.findById(eventId)
                        .map(event -> {
                            compte.removeFavori(event);
                            compteRepository.save(compte);
                            return ResponseEntity.ok(Map.of("message", "Favori supprimé avec succès"));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getById(@PathVariable Long id) {
        return repo.findByIdWithOrganisationAndPole(id)
                .map(EventDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orga/{organisationId}")
    public ResponseEntity<List<EventDTO>> getByOrganisation(@PathVariable Long organisationId) {
        List<EventDTO> events = toSortedDtos(repo.findByOrganisationIdWithOrganisationAndPole(organisationId));
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        return repo.findById(id)
                .map(existingEvent -> {
                    // Mise à jour des champs
                    existingEvent.setTitle(eventDetails.getTitle());
                    existingEvent.setDescription(eventDetails.getDescription());
                    existingEvent.setAddress(eventDetails.getAddress());
                    existingEvent.setBeginDate(eventDetails.getBeginDate());
                    existingEvent.setEndDate(eventDetails.getEndDate());
                    existingEvent.setCapacity(eventDetails.getCapacity());
                    existingEvent.setStatus(eventDetails.getStatus());
                    existingEvent.setPrice(eventDetails.getPrice());

                    Event updatedEvent = repo.save(existingEvent);
                    return repo.findByIdWithOrganisationAndPole(updatedEvent.getId())
                            .map(EventDTO::from)
                            .map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.ok(EventDTO.from(updatedEvent)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteFavoritesByEventId(id);
        repo.deleteInscriptionsByEventId(id);
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Événement supprimé avec succès"));
    }

    private static List<EventDTO> toSortedDtos(List<Event> events) {
        return events.stream()
                .sorted((e1, e2) -> {
                    // Trier par date de début descendante (plus récent en premier)
                    if (e1.getBeginDate() != null && e2.getBeginDate() != null) {
                        return e2.getBeginDate().compareTo(e1.getBeginDate());
                    }
                    // Si une date est null, mettre les événements avec date en premier
                    if (e1.getBeginDate() == null && e2.getBeginDate() != null) {
                        return 1;
                    }
                    if (e1.getBeginDate() != null && e2.getBeginDate() == null) {
                        return -1;
                    }
                    return 0;
                })
                .map(EventDTO::from)
                .toList();
    }

    private static boolean isOtherUser(Long userId, AuthenticatedUser authenticatedUser) {
        return authenticatedUser != null && !userId.equals(authenticatedUser.id());
    }
}
