package fr.mif10.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import fr.mif10.backend.entity.UserRole;
import fr.mif10.backend.repository.CompteRepository;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:uniflow-init-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
class DatabaseInitializationTest {

    @Autowired
    private CompteRepository compteRepository;

    @Test
    @Transactional
    void shouldLoadDefaultAccountsWithCurrentDatabaseModel() {
        assertThat(compteRepository.findByEmail("admin@univ-lyon1.fr"))
                .get()
                .extracting(account -> account.getRoles().contains(UserRole.ADMIN))
                .isEqualTo(true);

        assertThat(compteRepository.findByEmail("organisateur@univ-lyon1.fr"))
                .get()
                .satisfies(account -> {
                    assertThat(account.getRoles()).contains(UserRole.ORGANISATEUR);
                    assertThat(account.getOrganisations())
                            .extracting("nomStructure")
                            .contains("Club Informatique");
                });
    }
}
