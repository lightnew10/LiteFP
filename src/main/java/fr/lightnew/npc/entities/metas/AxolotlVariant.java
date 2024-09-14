package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum AxolotlVariant {
    LUCY(0),
    WILD(1),
    GOLD(2),
    CYAN(3),
    BLUE(4);

    @Getter
    private final int id;

    AxolotlVariant(int id) {
        this.id = id;
    }
}
