package fr.lightnew.npc.tools;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

public class ServerUtils {
    public static MinecraftServer getMinecraftServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    public static ServerLevel getServerLevel() {
        return ((CraftServer) Bukkit.getServer()).getServer().overworld();
    }
}
