package fr.mif10.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fr.mif10.backend.entity.Compte;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;

class OrganisationDTOTest {

    @Test
    void shouldConvertOrganisationToDto() {
        Pole pole = new Pole("Informatique");
        Organisation organisation = new Organisation("Club Info", pole);
        organisation.setId(12L);
        organisation.addMembre(new Compte("club.info@univ-lyon1.fr", "secret", "Info", "Club"));

        OrganisationDTO dto = OrganisationDTO.from(organisation);

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.name()).isEqualTo("Club Info");
        assertThat(dto.nomStructure()).isEqualTo("Club Info");
        assertThat(dto.email()).isEqualTo("club.info@univ-lyon1.fr");
        assertThat(dto.pole()).isEqualTo("Informatique");
        assertThat(dto.status()).isEqualTo("Actif");
    }

    @Test
    void shouldHandleNullOrganisationAndMissingRelations() {
        Organisation organisation = new Organisation("Sans pole", null);

        assertThat(OrganisationDTO.from(null)).isNull();

        OrganisationDTO dto = OrganisationDTO.from(organisation);
        assertThat(dto.pole()).isNull();
        assertThat(dto.poleId()).isNull();
        assertThat(dto.email()).isNull();
    }

    @Test
    void shouldResolveNameFromNameOrNomStructure() {
        OrganisationDTO fromName = new OrganisationDTO(null, "Club Info", null, null, null, null, null);
        OrganisationDTO fromNomStructure = new OrganisationDTO(null, "", "BDE", null, null, null, null);

        assertThat(fromName.resolvedName()).isEqualTo("Club Info");
        assertThat(fromNomStructure.resolvedName()).isEqualTo("BDE");
    }
}
