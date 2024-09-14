package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum CreeperVariant {
    IDLE(-1),
    FUSE(1);

    @Getter
    private final int id;

    CreeperVariant(int id) {
        this.id = id;
    }
}
