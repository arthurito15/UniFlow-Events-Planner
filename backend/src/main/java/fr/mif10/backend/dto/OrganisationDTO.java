package fr.mif10.backend.dto;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;

/**
 * DTO exposé pour les organisations dans l'interface admin.
 *
 * @param id identifiant de l'organisation
 * @param name nom compatible avec le front
 * @param nomStructure nom métier de l'organisation
 * @param email email du premier membre rattaché
 * @param pole nom du pôle rattaché
 * @param poleId identifiant du pôle rattaché
 * @param status statut affiché par le front
 */
public record OrganisationDTO(
        Long id,
        String name,
        String nomStructure,
        String email,
        String pole,
        Long poleId,
        String status
) {

    /**
     * Convertit une entité organisation en réponse API.
     */
    public static OrganisationDTO from(Organisation organisation) {
        if (organisation == null) {
            return null;
        }

        Pole pole = organisation.getPole();
        String memberEmail = organisation.getMembres()
                .stream()
                .findFirst()
                .map(Compte::getEmail)
                .orElse(null);

        return new OrganisationDTO(
                organisation.getId(),
                organisation.getNomStructure(),
                organisation.getNomStructure(),
                memberEmail,
                pole != null ? pole.getName() : null,
                pole != null ? pole.getId() : null,
                "Actif"
        );
    }

    /**
     * Résout le nom envoyé par le front, compatible avec name et nomStructure.
     */
    public String resolvedName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return nomStructure;
    }
}
