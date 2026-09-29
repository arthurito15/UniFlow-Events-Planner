package fr.mif10.backend.dto;

public record RegisterRequest(
        String nom,
        String prenom,
        String email,
        String password
) {}
