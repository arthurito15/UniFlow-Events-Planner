package fr.mif10.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.mif10.backend.entity.Organisation;

/**
 * Accès aux organisations.
 */
@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    /**
     * Trouver une organisation par son nom.
     */
    Optional<Organisation> findByNomStructureIgnoreCase(String nomStructure);

    boolean existsByPoleId(Long poleId);

    /**
     * Récupérer toutes les organisations avec leur pôle et leurs membres.
     */
    @Query("""
            SELECT DISTINCT o FROM Organisation o
            LEFT JOIN FETCH o.pole
            LEFT JOIN FETCH o.membres
            """)
    List<Organisation> findAllWithPoleAndMembers();

    /**
     * Récupérer une organisation avec son pôle et ses membres.
     */
    @Query("""
            SELECT DISTINCT o FROM Organisation o
            LEFT JOIN FETCH o.pole
            LEFT JOIN FETCH o.membres
            WHERE o.id = :id
            """)
    Optional<Organisation> findByIdWithPoleAndMembers(@Param("id") Long id);

    /**
     * Supprimer les favoris liés aux événements d'une organisation.
     */
    @Modifying
    @Query(value = """
            DELETE FROM favoris
            WHERE event_id IN (
                SELECT id FROM event WHERE organisation_id = :organisationId
            )
            """, nativeQuery = true)
    void deleteFavoritesByOrganisationId(@Param("organisationId") Long organisationId);

    /**
     * Supprimer les inscriptions liées aux événements d'une organisation.
     */
    @Modifying
    @Query(value = """
            DELETE FROM inscription
            WHERE event_id IN (
                SELECT id FROM event WHERE organisation_id = :organisationId
            )
            """, nativeQuery = true)
    void deleteInscriptionsByOrganisationId(@Param("organisationId") Long organisationId);
}
