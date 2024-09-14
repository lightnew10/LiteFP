package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum LlamaVariant {
    CREAMY(0),
    WHITE(1),
    BROWN(2),
    GRAY(3);

    @Getter
    private final int id;

    LlamaVariant(int id) {
        this.id = id;
    }
}
