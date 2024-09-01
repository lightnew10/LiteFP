package fr.lightnew.npc.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EffectNPC {
    private Boolean lookLock;
    private Boolean collides;

    public EffectNPC () {
        this.lookLock = true;
        this.collides = true;
    }

    public EffectNPC(Boolean lookLock, Boolean collible) {
        this.lookLock = lookLock;
        this.collides = collible;
    }

    @Override
    public String toString() {
        return "EffectNPC{" +
                "lookLock=" + lookLock +
                ", collides=" + collides +
                '}';
    }
}
