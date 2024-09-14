package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum RabbitVariant {
    BROWN(0),
    BLACK_AND_WHITE(3),
    GOLD(4),
    CREAM(5),
    KILLER_BUNNY(99);

    @Getter
    private final int id;

    RabbitVariant(int id) {
        this.id = id;
    }
}
