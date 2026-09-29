package fr.mif10.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
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

import fr.mif10.backend.dto.OrganisationDTO;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;
import fr.mif10.backend.entity.UserRole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.OrganisationRepository;
import fr.mif10.backend.repository.PoleRepository;

/**
 * Endpoints admin pour gérer les organisations.
 */
@RestController
@RequestMapping("/api/organizers")
public class OrganisationController {

    private final OrganisationRepository organisationRepository;
    private final PoleRepository poleRepository;
    private final CompteRepository compteRepository;

    /**
     * Crée le controller avec les repositories nécessaires.
     */
    public OrganisationController(
            OrganisationRepository organisationRepository,
            PoleRepository poleRepository,
            CompteRepository compteRepository
    ) {
        this.organisationRepository = organisationRepository;
        this.poleRepository = poleRepository;
        this.compteRepository = compteRepository;
    }

    /**
     * Liste toutes les organisations.
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrganisationDTO>> getAll() {
        List<OrganisationDTO> organisations = organisationRepository.findAllWithPoleAndMembers()
                .stream()
                .map(OrganisationDTO::from)
                .toList();
        return ResponseEntity.ok(organisations);
    }

    /**
     * Récupère une organisation par son identifiant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganisationDTO> getById(@PathVariable Long id) {
        return organisationRepository.findByIdWithPoleAndMembers(id)
                .map(OrganisationDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une organisation.
     */
    @PostMapping("/create")
    public ResponseEntity<OrganisationDTO> create(@RequestBody OrganisationDTO request) {
        String name = request != null ? request.resolvedName() : null;
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (organisationRepository.findByNomStructureIgnoreCase(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Pole pole = findPole(request);
        if (pole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Organisation organisation = new Organisation(name, pole);
        attachExistingMember(organisation, request.email());

        Organisation savedOrganisation = organisationRepository.save(organisation);
        return organisationRepository.findByIdWithPoleAndMembers(savedOrganisation.getId())
                .map(OrganisationDTO::from)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(OrganisationDTO.from(savedOrganisation)));
    }

    /**
     * Met à jour une organisation.
     */
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<OrganisationDTO> update(
            @PathVariable Long id,
            @RequestBody OrganisationDTO request
    ) {
        return organisationRepository.findByIdWithPoleAndMembers(id)
                .map(organisation -> updateOrganisation(organisation, request))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Supprime une organisation et les données de liaison de ses événements.
     */
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!organisationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        organisationRepository.deleteFavoritesByOrganisationId(id);
        organisationRepository.deleteInscriptionsByOrganisationId(id);
        organisationRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Organisation supprimée avec succès"));
    }

    private Pole findPole(OrganisationDTO request) {
        if (request.poleId() != null) {
            return poleRepository.findById(request.poleId()).orElse(null);
        }
        if (request.pole() != null && !request.pole().isBlank()) {
            return poleRepository.findByName(request.pole()).orElse(null);
        }
        return null;
    }

    private ResponseEntity<OrganisationDTO> updateOrganisation(
            Organisation organisation,
            OrganisationDTO request
    ) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String name = request.resolvedName();
        if (name != null && !name.isBlank()) {
            Optional<Organisation> existingOrganisation =
                    organisationRepository.findByNomStructureIgnoreCase(name);
            if (existingOrganisation.isPresent() &&
                    !existingOrganisation.get().getId().equals(organisation.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            organisation.setNomStructure(name);
        }

        if (request.poleId() != null || (request.pole() != null && !request.pole().isBlank())) {
            Pole pole = findPole(request);
            if (pole == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            organisation.setPole(pole);
        }

        attachExistingMember(organisation, request.email());
        Organisation savedOrganisation = organisationRepository.save(organisation);
        return organisationRepository.findByIdWithPoleAndMembers(savedOrganisation.getId())
                .map(OrganisationDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(OrganisationDTO.from(savedOrganisation)));
    }

    private void attachExistingMember(Organisation organisation, String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        compteRepository.findByEmail(email)
                .ifPresent(compte -> {
                    organisation.addMembre(compte);
                    compte.addRole(UserRole.ORGANISATEUR);
                    compte.addOrganisation(organisation);
                    compteRepository.save(compte);
                });
    }
}
