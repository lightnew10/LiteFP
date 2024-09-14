package fr.lightnew.npc.entities.metas;

import lombok.Getter;

public enum ParrotColor {
    RED_BLUE(0),
    BLUE(1),
    GREEN(2),
    YELLOW_BLUE(3),
    GREY(4);

    @Getter
    private int id;

    ParrotColor(int id) {
        this.id = id;
    }
}
