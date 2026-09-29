package fr.mif10.backend.dto;

import fr.mif10.backend.entity.Pole;

public record PoleDTO(
        Long id,
        String name
) {

    public static PoleDTO from(Pole pole) {
        if (pole == null) {
            return null;
        }
        return new PoleDTO(pole.getId(), pole.getName());
    }

    public Pole toEntity() {
        Pole pole = new Pole(this.name);
        return pole;
    }
}

