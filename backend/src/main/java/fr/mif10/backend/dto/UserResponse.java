package fr.mif10.backend.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        LocalDateTime dateInscription
) {}
