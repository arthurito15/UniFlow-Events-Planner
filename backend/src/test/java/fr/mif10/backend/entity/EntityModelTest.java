package fr.mif10.backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EntityModelTest {

    @Test
    void shouldManageCompteRolesFavoritesAndOrganisations() {
        Compte compte = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        Event event = event("Forum associations");
        Organisation organisation = new Organisation("BDE Info", new Pole("Informatique"));

        compte.addRole(UserRole.ORGANISATEUR);
        compte.addRole(UserRole.ORGANISATEUR);
        compte.removeRole(UserRole.UTILISATEUR);
        compte.addFavori(event);
        compte.addFavori(event);
        compte.removeFavori(event);
        compte.addOrganisation(organisation);
        compte.addOrganisation(organisation);
        compte.prePersist();

        assertThat(compte.getRoles()).containsExactly(UserRole.ORGANISATEUR);
        assertThat(compte.getEventFavoris()).isEmpty();
        assertThat(compte.getOrganisations()).containsExactly(organisation);
        assertThat(compte.getDateInscription()).isNotNull();

        compte.setEmail("new@univ-lyon1.fr");
        compte.setPasswordHash("new-secret");
        compte.setNom("Martin");
        compte.setPrenom("Bob");
        compte.setDateInscription(LocalDateTime.of(2026, 1, 2, 3, 4));
        compte.setRoles(new HashSet<>(List.of(UserRole.ADMIN)));
        compte.setEventFavoris(new HashSet<>(List.of(event)));
        compte.setOrganisations(new HashSet<>(List.of(organisation)));

        assertThat(compte.getEmail()).isEqualTo("new@univ-lyon1.fr");
        assertThat(compte.getPasswordHash()).isEqualTo("new-secret");
        assertThat(compte.getNom()).isEqualTo("Martin");
        assertThat(compte.getPrenom()).isEqualTo("Bob");
        assertThat(compte.getDateInscription()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4));
        assertThat(compte.getRoles()).containsExactly(UserRole.ADMIN);
        assertThat(compte.getEventFavoris()).containsExactly(event);
        assertThat(compte.getOrganisations()).containsExactly(organisation);
    }

    @Test
    void shouldExposeEventFields() {
        Organisation organisation = new Organisation("BDE Info", new Pole("Informatique"));
        Pole pole = new Pole("Culture");
        Event event = event("Ancien titre");

        event.setTitle("Nouveau titre");
        event.setDescription("Nouvelle description");
        event.setAddress("Campus");
        event.setBeginDate(LocalDateTime.of(2026, 5, 1, 10, 0));
        event.setEndDate(LocalDateTime.of(2026, 5, 1, 12, 0));
        event.setCapacity(42);
        event.setStatus(EventStatus.CANCELED);
        event.setPrice(12.5);
        event.setOrganisation(organisation);
        event.setPole(pole);

        assertThat(event.getTitle()).isEqualTo("Nouveau titre");
        assertThat(event.getDescription()).isEqualTo("Nouvelle description");
        assertThat(event.getAddress()).isEqualTo("Campus");
        assertThat(event.getBeginDate()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
        assertThat(event.getEndDate()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 0));
        assertThat(event.getCapacity()).isEqualTo(42);
        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELED);
        assertThat(event.getPrice()).isEqualTo(12.5);
        assertThat(event.getOrganisation()).isEqualTo(organisation);
        assertThat(event.getPole()).isEqualTo(pole);
    }

    @Test
    void shouldManageOrganisationMembers() {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE Info", pole);
        Compte compte = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");

        organisation.setId(12L);
        organisation.setNomStructure("Club Info");
        organisation.setPole(pole);
        organisation.addMembre(compte);
        organisation.addMembre(compte);

        assertThat(organisation.getId()).isEqualTo(12L);
        assertThat(organisation.getNomStructure()).isEqualTo("Club Info");
        assertThat(organisation.getPole()).isEqualTo(pole);
        assertThat(organisation.getMembres()).containsExactly(compte);
        assertThat(organisation.getEvents()).isEmpty();

        organisation.setMembres(new HashSet<>());
        assertThat(organisation.getMembres()).isEmpty();
    }

    @Test
    void shouldExposePoleAndInscriptionFields() {
        Pole pole = new Pole("Informatique");
        pole.setName("Sciences");
        ReflectionTestUtils.setField(pole, "id", 9L);

        Compte compte = new Compte("alice@univ-lyon1.fr", "secret123", "Dupont", "Alice");
        Event event = event("Forum associations");
        Inscription inscription = new Inscription(compte, event);

        inscription.setUser(compte);
        inscription.setEvent(event);

        assertThat(pole.getId()).isEqualTo(9L);
        assertThat(pole.getName()).isEqualTo("Sciences");
        assertThat(inscription.getUser()).isEqualTo(compte);
        assertThat(inscription.getEvent()).isEqualTo(event);
    }

    private static Event event(String title) {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("BDE Info", pole);
        return new Event(
                title,
                "Description",
                "Campus LyonTech",
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 16, 0),
                300,
                EventStatus.PUBLISHED,
                0.0,
                organisation,
                pole
        );
    }
}
