package fr.mif10.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mif10.backend.dto.AdminUserDTO;
import fr.mif10.backend.dto.UserResponse;
import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.UserRole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.repository.OrganisationRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CompteRepository compteRepository;
    private final OrganisationRepository organisationRepository;

    public UserController(
            CompteRepository compteRepository,
            OrganisationRepository organisationRepository
    ) {
        this.compteRepository = compteRepository;
        this.organisationRepository = organisationRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminUserDTO>> getAll() {
        List<AdminUserDTO> users = compteRepository.findAllWithOrganisations()
                .stream()
                .map(AdminUserDTO::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        return new UserResponse(
                compte.getId(),
                compte.getNom(),
                compte.getPrenom(),
                compte.getEmail(),
                compte.getDateInscription()
        );
    }

    @Transactional
    @PutMapping("/{id}/organization")
    public ResponseEntity<AdminUserDTO> updateOrganization(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        return compteRepository.findByIdWithOrganisations(id)
                .map(compte -> updateUserOrganizations(compte, request))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!compteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        compteRepository.deleteFavoritesByCompteId(id);
        compteRepository.deleteInscriptionsByCompteId(id);
        compteRepository.deleteOrganisationMembershipsByCompteId(id);
        compteRepository.deleteRolesByCompteId(id);
        compteRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
    }

    private ResponseEntity<AdminUserDTO> updateUserOrganizations(
            Compte compte,
            Map<String, Object> request
    ) {
        List<String> organizationNames = resolveOrganizationNames(request);
        List<Organisation> organisations = new ArrayList<>();

        for (String organizationName : organizationNames) {
            Organisation organisation = organisationRepository.findByNomStructureIgnoreCase(organizationName)
                    .orElse(null);
            if (organisation == null) {
                return ResponseEntity.notFound().build();
            }
            organisations.add(organisation);
        }

        removeOrganisationRole(compte);

        if (!organisations.isEmpty()) {
            compte.addRole(UserRole.ORGANISATEUR);
        }
        for (Organisation organisation : organisations) {
            organisation.addMembre(compte);
            compte.addOrganisation(organisation);
            organisationRepository.save(organisation);
        }

        Compte savedCompte = compteRepository.save(compte);
        return ResponseEntity.ok(AdminUserDTO.from(savedCompte));
    }

    private List<String> resolveOrganizationNames(Map<String, Object> request) {
        if (request == null) {
            return List.of();
        }
        if (request.containsKey("organizations")) {
            Object organizations = request.get("organizations");
            if (organizations instanceof List<?> values) {
                return values.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .distinct()
                        .toList();
            }
            return List.of();
        }

        Object organization = request.get("organization");
        if (organization == null || organization.toString().isBlank()) {
            return List.of();
        }
        return List.of(organization.toString().trim());
    }

    private void removeOrganisationRole(Compte compte) {
        for (Organisation organisation : List.copyOf(compte.getOrganisations())) {
            organisation.getMembres().remove(compte);
            organisationRepository.save(organisation);
        }
        compte.getOrganisations().clear();
        compte.removeRole(UserRole.ORGANISATEUR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException exception) {
        return ResponseEntity.status(404).body(Map.of("message", exception.getMessage()));
    }
}
