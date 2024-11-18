package fr.lightnew.npc.events.builder;

import fr.lightnew.npc.entities.npc.NPCCreator;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerNPCUnLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final NPCCreator npc;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public PlayerNPCUnLoadEvent(Player player, NPCCreator npc) {
        this.npc = npc;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
