package fr.mif10.backend.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.UserRole;

/**
 * DTO exposé pour la page admin des utilisateurs.
 *
 * @param id identifiant utilisateur
 * @param name nom complet affiché
 * @param email email utilisateur
 * @param roles rôles affichables par le front
 * @param organization première organisation rattachée
 * @param organizations toutes les organisations rattachées
 * @param registrationDate date d'inscription formatée
 */
public record AdminUserDTO(
        Long id,
        String name,
        String email,
        List<String> roles,
        String organization,
        List<String> organizations,
        String registrationDate
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Convertit un compte en réponse admin.
     */
    public static AdminUserDTO from(Compte compte) {
        String name = "%s %s".formatted(
                compte.getPrenom() != null ? compte.getPrenom() : "",
                compte.getNom() != null ? compte.getNom() : ""
        ).trim();
        if (name.isBlank()) {
            name = compte.getEmail();
        }

        List<String> organizations = compte.getOrganisations()
                .stream()
                .map(Organisation::getNomStructure)
                .sorted()
                .toList();
        String organization = organizations.stream().findFirst().orElse("");

        String registrationDate = "";
        if (compte.getDateInscription() != null) {
            registrationDate = compte.getDateInscription().format(DATE_FORMATTER);
        }

        return new AdminUserDTO(
                compte.getId(),
                name,
                compte.getEmail(),
                compte.getRoles().stream().map(AdminUserDTO::toDisplayRole).toList(),
                organization,
                organizations,
                registrationDate
        );
    }

    private static String toDisplayRole(UserRole role) {
        return switch (role) {
            case ADMIN -> "Admin";
            case ORGANISATEUR -> "Organisateur";
            case UTILISATEUR -> "Utilisateur";
        };
    }
}
