package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum MooshroomVariant {
    RED("red"),
    BROWN("brown");

    @Getter
    private final String variant;

    MooshroomVariant(String variant) {
        this.variant = variant;
    }
}
