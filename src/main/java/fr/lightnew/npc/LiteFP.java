package fr.lightnew.npc;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.commands.RecordCommand;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.events.PlayerManager;
import fr.lightnew.npc.sql.RequestNPC;
import fr.lightnew.npc.tools.ConsoleLog;
import fr.lightnew.npc.tools.WorkerLoadNPC;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class LiteFP extends JavaPlugin {

    public static LiteFP instance;
    public static Map<Integer, NPCCreator> list_npc;
    public static Map<Integer, NPCCreator> list_waiting_update_npc;
    public static final Map<String, String[]> skinCache = new HashMap<>();
    @Getter
    private static Connection connection;
    public static WorkerLoadNPC workerLoadNPC;
    @Getter
    private ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        createDataBase();
        connectSQL();
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onEnable() {
        ConsoleLog.info("Plugin starting...");
        list_npc = RequestNPC.getAllNPC();
        list_waiting_update_npc = new HashMap<>();
        workerLoadNPC = new WorkerLoadNPC();
        workerLoadNPC.runTaskTimerAsynchronously(this, 0, 10);
        updateWaitingList();

        /*
        Commands
        */
        getCommand("npc").setExecutor(new CommandNPC());
        getCommand("npc").setTabCompleter(new CommandNPC());
        getCommand("record").setExecutor(new RecordCommand());
        getCommand("record").setTabCompleter(new RecordCommand());

        /*
        Listener Events
        */
        Bukkit.getPluginManager().registerEvents(new PlayerManager(), this);

        ConsoleLog.success("Plugin stated");
    }

    @Override
    public void onDisable() {
        if (!Bukkit.getOnlinePlayers().isEmpty())
            Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Reload in progress..."));
        list_waiting_update_npc.values().forEach(RequestNPC::updateNPC);
        list_npc.values().forEach(RequestNPC::updateNPC);
        ConsoleLog.success("Plugin stopped");
    }

    private static boolean connectSQL() {
        final String url = "jdbc:mysql://" + instance.getConfig().getString("database.host") + ":" + instance.getConfig().getInt("database.port") + "/" + instance.getConfig().getString("database.database") + "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
        try {
            connection = DriverManager.getConnection(url, instance.getConfig().getString("database.username"), instance.getConfig().getString("database.passwd"));
            RequestNPC.createTable();
            connection.setAutoCommit(true);
            ConsoleLog.success("SQL Connected");
        }
        catch (SQLException e) {
            ConsoleLog.error("SQL connection not established");
            ConsoleLog.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        return true;
    }
    private static void createDataBase() {
        final String url = "jdbc:mysql://" + instance.getConfig().getString("database.host") + ":" + instance.getConfig().getInt("database.port") + "/"  + "?useUnicode=true&characterEncoding=UTF-8";
        try {
            connection = DriverManager.getConnection(url, instance.getConfig().getString("database.username"), instance.getConfig().getString("database.passwd"));
            String query = "CREATE DATABASE IF NOT EXISTS " + instance.getConfig().getString("database.database");
            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            connection.setAutoCommit(true);
            ConsoleLog.success("Database created");
        }
        catch (SQLException e) {
            ConsoleLog.error("Database error");
            ConsoleLog.error(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void updateWaitingList() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            //ConsoleLog.info("Updating data...");
            list_waiting_update_npc.values().forEach(RequestNPC::updateNPC);
            list_waiting_update_npc.clear();
            //ConsoleLog.success("NPC's updated");
        }, 0, (20 * 30));
    }
}
