package fr.mif10.backend.dto;

public record AuthRequest(
        String email,
        String password
) {}
