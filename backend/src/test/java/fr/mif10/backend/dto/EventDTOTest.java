package fr.mif10.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import fr.mif10.backend.entity.Event;
import fr.mif10.backend.entity.EventStatus;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;

class EventDTOTest {

    @Test
    void shouldMapEventRelationsWhenPresent() {
        Pole pole = new Pole("Informatique");
        ReflectionTestUtils.setField(pole, "id", 7L);
        Organisation organisateur = new Organisation("BDE Info", pole);
        organisateur.setId(12L);
        Event event = event(organisateur, pole);

        EventDTO dto = EventDTO.from(event);

        assertThat(dto.title()).isEqualTo("Forum associations");
        assertThat(dto.organisationId()).isEqualTo(12L);
        assertThat(dto.organisationName()).isEqualTo("BDE Info");
        assertThat(dto.poleId()).isEqualTo(7L);
        assertThat(dto.poleName()).isEqualTo("Informatique");
    }

    @Test
    void shouldMapNullRelationsToNullValues() {
        Event event = event(null, null);

        EventDTO dto = EventDTO.from(event);

        assertThat(dto.organisationId()).isNull();
        assertThat(dto.organisationName()).isNull();
        assertThat(dto.poleId()).isNull();
        assertThat(dto.poleName()).isNull();
    }

    private static Event event(Organisation organisation, Pole pole) {
        return new Event(
                "Forum associations",
                "Presentation des associations",
                "Atrium",
                LocalDateTime.of(2026, 10, 5, 10, 0),
                LocalDateTime.of(2026, 10, 5, 16, 0),
                300,
                EventStatus.CLOSED,
                0.0,
                organisation,
                pole
        );
    }
}
