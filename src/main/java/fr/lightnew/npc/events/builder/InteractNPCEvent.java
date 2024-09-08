package fr.lightnew.npc.events.builder;

import fr.lightnew.npc.entities.npc.NPCCreator;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

@Getter
public class InteractNPCEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ClickType clickType;
    private final NPCCreator npcManager;
    private boolean isCancelled;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public InteractNPCEvent(Player player, ClickType clickType, NPCCreator npcManager) {
        this.player = player;
        this.clickType = clickType;
        this.npcManager = npcManager;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
