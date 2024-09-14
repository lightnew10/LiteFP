package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum FoxVariant {
    RED(0),
    SNOW(1);

    @Getter
    private final int id;

    FoxVariant(int id) {
        this.id = id;
    }
}
