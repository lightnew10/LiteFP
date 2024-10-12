package fr.lightnew.npc.entities.metas;

import lombok.Getter;

@Getter
public enum AnimationNPC {
    SWING_MAIN_HAND(0, "FIGHT"),
    HURT(1, "HURT"),
    LEAVE_BED(2, "LEAVE_BED"),
    SWING_OFF_HAND(3, "FIGHT_SECOND_HAND"),
    CRITICAL_EFFECT(4, "CRITICAL_EFFECT"),
    MAGICAL_CRITICAL_EFFECT(5, "MAGICAL_CRITICAL_EFFECT");

    private int id;
    private String name;

    AnimationNPC(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
