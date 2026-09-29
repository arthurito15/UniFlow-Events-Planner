package fr.mif10.backend.dto;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Inscription;

/**
 * DTO exposé pour les participants d'un événement.
 *
 * @param id identifiant utilisateur
 * @param nom nom du participant
 * @param prenom prénom du participant
 * @param email email du participant
 * @param eventId identifiant de l'événement
 */
public record ParticipantDTO(
        Long id,
        String nom,
        String prenom,
        String email,
        Long eventId
) {

    /**
     * Convertit une inscription en participant.
     */
    public static ParticipantDTO from(Inscription inscription) {
        Compte user = inscription.getUser();
        return new ParticipantDTO(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                inscription.getEvent().getId()
        );
    }
}
