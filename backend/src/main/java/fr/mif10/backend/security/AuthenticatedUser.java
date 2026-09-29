package fr.mif10.backend.security;

import java.util.List;

public record AuthenticatedUser(
        Long id,
        String email,
        List<String> roles
) {}
