package fr.mif10.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.mif10.backend.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByPoleId(Long poleId);

    @Query("SELECT e FROM Event e JOIN FETCH e.organisation LEFT JOIN FETCH e.pole")
    List<Event> findAllWithOrganisationAndPole();

    @Query("SELECT e FROM Event e JOIN FETCH e.organisation LEFT JOIN FETCH e.pole WHERE e.pole.name = :poleName")
    List<Event> findByPoleName(@Param("poleName") String poleName);

    @Query("SELECT e FROM Event e JOIN FETCH e.organisation LEFT JOIN FETCH e.pole WHERE e.id = :id")
    Optional<Event> findByIdWithOrganisationAndPole(@Param("id") Long id);

    @Query("""
            SELECT e FROM Event e
            JOIN FETCH e.organisation
            LEFT JOIN FETCH e.pole
            WHERE e.organisation.id = :organisationId
            """)
    List<Event> findByOrganisationIdWithOrganisationAndPole(@Param("organisationId") Long organisationId);

    @Modifying
    @Query(value = "DELETE FROM favoris WHERE event_id = :eventId", nativeQuery = true)
    void deleteFavoritesByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query(value = "DELETE FROM inscription WHERE event_id = :eventId", nativeQuery = true)
    void deleteInscriptionsByEventId(@Param("eventId") Long eventId);
}
