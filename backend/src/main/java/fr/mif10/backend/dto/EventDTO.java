package fr.mif10.backend.dto;

import java.time.LocalDateTime;

import fr.mif10.backend.entity.Event;
import fr.mif10.backend.entity.EventStatus;
import fr.mif10.backend.entity.Organisation;
import fr.mif10.backend.entity.Pole;

public record EventDTO(
        Long id,
        String title,
        String description,
        String address,
        LocalDateTime beginDate,
        LocalDateTime endDate,
        Integer capacity,
        EventStatus status,
        Double price,
        Long organisationId,
        String organisationName,
        Long poleId,
        String poleName
) {

    public static EventDTO from(Event event) {
        Organisation organisation = event.getOrganisation();
        Pole pole = event.getPole();

        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getAddress(),
                event.getBeginDate(),
                event.getEndDate(),
                event.getCapacity(),
                event.getStatus(),
                event.getPrice(),
                organisation != null ? organisation.getId() : null,
                organisation != null ? organisation.getNomStructure() : null,
                pole != null ? pole.getId() : null,
                pole != null ? pole.getName() : null
        );
    }
}
