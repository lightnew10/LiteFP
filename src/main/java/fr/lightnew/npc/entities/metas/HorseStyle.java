package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum HorseStyle {
    NONE(0),
    WHITE_SOCKS(1),
    WHITE_SPOTS(2),
    WHITE_LARGER_SPOTS(3),
    WHITEFIELD(4);

    @Getter
    private final int id;

    HorseStyle(int id) {
        this.id = id;
    }
}
