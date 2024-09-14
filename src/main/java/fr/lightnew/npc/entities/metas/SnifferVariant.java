package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum SnifferVariant {
    IDLING(0),
    FEELING_HAPPY(1),
    SCENTING(2),
    SNIFFING(3),
    SEARCHING(4),
    DIGGING(5),
    RISING(6);

    @Getter
    private final int id;

    SnifferVariant(int id) {
        this.id = id;
    }
}
