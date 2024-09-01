package fr.lightnew.npc.entities;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;

import javax.xml.stream.Location;
import java.util.List;

@Getter
@Setter
public class HolographicCreator {

    private int id;
    private List<String> lines;
    private Location location;
    private int idNPC;

    public HolographicCreator(int id, int idNPC, Location location, String... lines) {
        this.id = id;
        this.idNPC = idNPC;
        this.location = location;
        this.lines = List.of(lines);
    }

    public void create(Player player) {
        EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public void addLine() {
    }

    public void removeLine() {
    }

    public void destroy() {
    }
}
