package fr.mif10.backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;


@Entity
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime dateInscription;

    private String nom;
    private String prenom;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "favoris",
            joinColumns = @JoinColumn(name = "compte_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> eventFavoris = new HashSet<>();

    @ManyToMany(mappedBy = "membres")
    private Set<Organisation> organisations = new HashSet<>();

    protected Compte() {}

    public Compte(String email, String passwordHash, String nom, String prenom) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nom = nom;
        this.prenom = prenom;
        this.roles.add(UserRole.UTILISATEUR);
    }

    @PrePersist
    public void prePersist() {
        this.dateInscription = LocalDateTime.now();
    }

    // ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // EMAIL
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // PASSWORD
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // ROLES (corrigé)
    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    // Helpers utiles (recommandé)
    public void addRole(UserRole role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    // DATE
    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    // NOM / PRENOM
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    // FAVORIS
    public Set<Event> getEventFavoris() {
        return eventFavoris;
    }

    public void setEventFavoris(Set<Event> eventFavoris) {
        this.eventFavoris = eventFavoris;
    }

    public void addFavori(Event event) {
        if (!this.eventFavoris.contains(event)) {
            this.eventFavoris.add(event);
        }
    }

    public void removeFavori(Event event) {
        this.eventFavoris.remove(event);
    }

    // ORGANISATIONS
    public Set<Organisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Set<Organisation> organisations) {
        this.organisations = organisations;
    }

    public void addOrganisation(Organisation organisation) {
        if (!this.organisations.contains(organisation)) {
            this.organisations.add(organisation);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Compte compte)) {
            return false;
        }
        return id != null && id.equals(compte.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
