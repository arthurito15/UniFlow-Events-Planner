package fr.mif10.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mif10.backend.dto.PoleDTO;
import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.repository.EventRepository;
import fr.mif10.backend.repository.OrganisationRepository;
import fr.mif10.backend.repository.PoleRepository;

@RestController
@RequestMapping("/api/poles")
public class PoleController {

    private final PoleRepository poleRepository;
    private final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    public PoleController(
            PoleRepository poleRepository,
            OrganisationRepository organisationRepository,
            EventRepository eventRepository
    ) {
        this.poleRepository = poleRepository;
        this.organisationRepository = organisationRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Récupérer tous les pôles.
     *
     * @return List de tous les pôles
     */
    @GetMapping("/all")
    public ResponseEntity<List<PoleDTO>> getAll() {
        List<PoleDTO> poles = poleRepository.findAll()
                .stream()
                .map(PoleDTO::from)
                .toList();
        return ResponseEntity.ok(poles);
    }

    /**
     * Récupérer un pôle par son ID.
     *
     * @param id ID du pôle
     * @return Le pôle correspondant ou 404 si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<PoleDTO> getById(@PathVariable Long id) {
        return poleRepository.findById(id)
                .map(PoleDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Créer un nouveau pôle (Admin only).
     *
     * @param poleDTO Le pôle à créer
     * @return Le pôle créé avec le statut 201 CREATED
     */
    @PostMapping("/create")
    public ResponseEntity<PoleDTO> create(@RequestBody PoleDTO poleDTO) {
        if (poleDTO == null || poleDTO.name() == null || poleDTO.name().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Vérifier que le pôle n'existe pas déjà
        boolean existsByName = poleRepository.findAll()
                .stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(poleDTO.name()));

        if (existsByName) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Pole pole = new Pole(poleDTO.name());
        Pole savedPole = poleRepository.save(pole);
        return ResponseEntity.status(HttpStatus.CREATED).body(PoleDTO.from(savedPole));
    }

    /**
     * Mettre à jour un pôle (Admin only).
     *
     * @param id      ID du pôle à mettre à jour
     * @param poleDTO Les nouvelles données du pôle
     * @return Le pôle mis à jour ou 404 si non trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<PoleDTO> update(@PathVariable Long id, @RequestBody PoleDTO poleDTO) {
        if (poleDTO == null || poleDTO.name() == null || poleDTO.name().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return poleRepository.findById(id)
                .map(existingPole -> {
                    existingPole.setName(poleDTO.name());
                    Pole updatedPole = poleRepository.save(existingPole);
                    return ResponseEntity.ok(PoleDTO.from(updatedPole));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprimer un pôle (Admin only).
     *
     * @param id ID du pôle à supprimer
     * @return Un message de confirmation ou 404 si non trouvé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!poleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (organisationRepository.existsByPoleId(id) || eventRepository.existsByPoleId(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ce pôle est encore utilisé."));
        }

        poleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Pôle supprimé avec succès"));
    }
}
