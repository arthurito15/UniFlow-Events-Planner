package fr.mif10.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mif10.backend.dto.AuthRequest;
import fr.mif10.backend.dto.AuthResponse;
import fr.mif10.backend.dto.RegisterRequest;
import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.UserRole;
import fr.mif10.backend.repository.CompteRepository;
import fr.mif10.backend.security.AuthenticatedUser;
import fr.mif10.backend.security.TokenService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String UNIVERSITY_DOMAIN = "univ-lyon1.fr";

    private final CompteRepository compteRepository;
    private final TokenService tokenService;

    public AuthController(CompteRepository compteRepository, TokenService tokenService) {
        this.compteRepository = compteRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        validateRegisterRequest(request);

        if (!request.email().endsWith(UNIVERSITY_DOMAIN)) {
            throw new IllegalArgumentException("L'adresse email doit appartenir au domaine universitaire.");
        }

        if (compteRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Un compte existe deja avec cet email.");
        }

        Compte compte = new Compte(
                request.email(),
                request.password(),
                request.nom(),
                request.prenom()
        );

        Compte saved = compteRepository.save(compte);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toAuthResponse(saved, "Inscription réussie."));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Email et mot de passe obligatoires.");
        }

        Compte compte = findByEmailWithOptionalOrganisations(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Identifiants invalides."));

        if (!request.password().equals(compte.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants invalides.");
        }

        return toAuthResponse(compte, "Connexion réussie.");
    }

    @GetMapping("/me")
    public ResponseEntity<Void> me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean accountExists = compteRepository.findById(authenticatedUser.id())
                .filter(compte -> compte.getEmail().equals(authenticatedUser.email()))
                .isPresent();

        if (!accountExists) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        String message = exception.getMessage();
        HttpStatus status = "Identifiants invalides.".equals(message) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.nom()) || isBlank(request.prenom()) ||
                isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Tous les champs sont obligatoires.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private AuthResponse toAuthResponse(Compte compte, String message) {
        List<String> roles = compte.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        List<String> organisations = List.of();
        if (compte.getRoles().contains(UserRole.ORGANISATEUR)) {
            organisations = compte.getOrganisations()
                    .stream()
                    .map(Organisation::getNomStructure)
                    .toList();
        }

        return new AuthResponse(
                compte.getId(),
                roles,
                resolveDisplayName(compte),
                compte.getEmail(),
                message,
                tokenService.generateToken(compte),
                organisations
        );
    }

    private Optional<Compte> findByEmailWithOptionalOrganisations(String email) {
        Optional<Compte> compteWithOrganisations = compteRepository.findByEmailWithOrganisations(email);
        if (compteWithOrganisations.isPresent()) {
            return compteWithOrganisations.or(() -> compteRepository.findByEmail(email));
        }

        return compteRepository.findByEmail(email);
    }

    private String resolveDisplayName(Compte compte) {
        if (compte.getPrenom() != null && compte.getNom() != null) {
            return compte.getPrenom() + " " + compte.getNom();
        }

        return "Administrateur";
    }
}
