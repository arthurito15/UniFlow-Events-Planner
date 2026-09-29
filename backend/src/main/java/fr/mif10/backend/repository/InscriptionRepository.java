package fr.mif10.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.mif10.backend.entity.Inscription;

/**
 * Accès aux inscriptions.
 */
@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    /**
     * Trouver une inscription utilisateur/événement.
     */
    Optional<Inscription> findByUserIdAndEventId(Long userId, Long eventId);

    /**
     * Récupérer les inscriptions d'un utilisateur avec les événements complets.
     */
    @Query("""
            SELECT DISTINCT i FROM Inscription i
            JOIN FETCH i.event e
            JOIN FETCH e.organisation
            LEFT JOIN FETCH e.pole
            WHERE i.user.id = :userId
            """)
    List<Inscription> findByUserIdWithEventDetails(@Param("userId") Long userId);

    /**
     * Récupérer les participants d'un événement.
     */
    @Query("""
            SELECT DISTINCT i FROM Inscription i
            JOIN FETCH i.user
            JOIN FETCH i.event
            WHERE i.event.id = :eventId
            """)
    List<Inscription> findByEventIdWithUser(@Param("eventId") Long eventId);
}
