package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum HorseVariant {
    WHITE(0),
    CREAMY(1),
    CHESTNUT(2),
    BROWN(3),
    BLACK(4),
    DARK_GREY(5),
    LIGHT_GREY(6);

    @Getter
    private final int id;

    HorseVariant(int id) {
        this.id = id;
    }
}
