package fr.lightnew.npc.commands;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.npc.NPCCreator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ClickSimulator {

    private static Vector getDirection(Player player) {
        return player.getLocation().getDirection();
    }

    public static NPCCreator getNPCFrontOfPlayer(Player player, double maxDistance) {
        Location playerLocation = player.getLocation();
        Vector direction = getDirection(player).normalize();

        for (NPCCreator entity : LiteFP.list_npc.values()) {
            if (entity.equals(player)) continue;

            Vector toEntity = entity.getLocation().toVector().subtract(playerLocation.toVector());
            double distance = toEntity.length();

            if (distance <= maxDistance && direction.dot(toEntity.normalize()) > 0.9) {
                return entity;
            }
        }

        return null;
    }
}


