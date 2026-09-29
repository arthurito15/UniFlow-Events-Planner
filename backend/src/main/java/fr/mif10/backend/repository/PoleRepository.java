package fr.mif10.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.mif10.backend.entity.Pole;

@Repository
public interface PoleRepository extends JpaRepository<Pole, Long> {

    /**
     * Trouver un pôle par son nom.
     * @param name Nom du pôle
     * @return Optional contenant le pôle s'il existe
     */
    Optional<Pole> findByName(String name);
}
