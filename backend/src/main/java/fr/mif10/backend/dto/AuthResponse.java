package fr.mif10.backend.dto;

import java.util.List;

public record AuthResponse(
        Long id,
        List<String> roles,
        String displayName,
        String email,
        String message,
        String token,
        List<String> organisations
) {}
