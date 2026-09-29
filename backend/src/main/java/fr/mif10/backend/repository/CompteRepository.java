package fr.mif10.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.mif10.backend.entity.Compte;

@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    Optional<Compte> findByEmail(String email);

    @Query("SELECT c FROM Compte c LEFT JOIN FETCH c.organisations WHERE c.email = :email")
    Optional<Compte> findByEmailWithOrganisations(@Param("email") String email);

    @Query("SELECT DISTINCT c FROM Compte c LEFT JOIN FETCH c.organisations")
    List<Compte> findAllWithOrganisations();

    @Query("SELECT DISTINCT c FROM Compte c LEFT JOIN FETCH c.organisations WHERE c.id = :id")
    Optional<Compte> findByIdWithOrganisations(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT c FROM Compte c
            LEFT JOIN FETCH c.eventFavoris e
            LEFT JOIN FETCH e.organisation
            LEFT JOIN FETCH e.pole
            WHERE c.id = :id
            """)
    Optional<Compte> findByIdWithEventFavoris(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM favoris WHERE compte_id = :compteId", nativeQuery = true)
    void deleteFavoritesByCompteId(@Param("compteId") Long compteId);

    @Modifying
    @Query(value = "DELETE FROM inscription WHERE user_id = :compteId", nativeQuery = true)
    void deleteInscriptionsByCompteId(@Param("compteId") Long compteId);

    @Modifying
    @Query(value = "DELETE FROM organisation_membres WHERE compte_id = :compteId", nativeQuery = true)
    void deleteOrganisationMembershipsByCompteId(@Param("compteId") Long compteId);

    @Modifying
    @Query(value = "DELETE FROM compte_roles WHERE compte_id = :compteId", nativeQuery = true)
    void deleteRolesByCompteId(@Param("compteId") Long compteId);
}
