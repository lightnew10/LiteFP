package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum SlimeVariant {
    TINY(1),
    SMALL(2),
    MEDIUM(4),
    LARGE(8);

    @Getter
    private final int size;

    SlimeVariant(int size) {
        this.size = size;
    }
}
