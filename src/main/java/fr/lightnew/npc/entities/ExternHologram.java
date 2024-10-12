package fr.lightnew.npc.entities;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.tools.ConsoleLog;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class ExternHologram {

    private final String name;
    private final List<String> lines;
    private Location location;

    public ExternHologram(String name, List<String> lines, Location location) {
        this.name = name;
        this.lines = lines;
        this.location = location;
    }

    public void save() {
        File file = new File(LiteFP.instance.getDataFolder().getParentFile() + "\\HolographicDisplays", "database.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration ymlConfig = YamlConfiguration.loadConfiguration(file);
        ymlConfig.set(name + ".position.world", location.getWorld().getName());
        ymlConfig.set(name + ".position.x", location.getX());
        ymlConfig.set(name + ".position.y", location.getY());
        ymlConfig.set(name + ".position.z", location.getZ());
        try {
            ymlConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLocation(Location location, double height) {
        if (location == null)
            throw new RuntimeException("Location is null");
        Location loc = location.clone();
        loc.setY(loc.getY() + height);
        this.location = loc;
        this.save();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holo reload");
        ConsoleLog.info("Plugin Holographic reloaded positon of hologram");
    }
}
