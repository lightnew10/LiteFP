package fr.lightnew.npc.sql;

import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.npc.EffectNPC;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.tools.ConsoleLog;
import fr.lightnew.npc.tools.ItemBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RequestNPC {

    private static String tableDB = "npc_storage";

    public static void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS " + tableDB + "(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "npc_name VARCHAR(260)," +
                "npc_skin_name VARCHAR(255)," +
                "disappear_range int," +
                "holo_linked_height double," +
                "linked_hologram VARCHAR(260)," +
                "npc_skin_texture TEXT," +
                "npc_skin_signature TEXT," +
                "location JSON," +
                "stuff JSON," +
                "effects JSON," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement(table);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, NPCCreator> getAllNPC() {
        Map<Integer, NPCCreator> NPCsCreator = new HashMap<>();
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement("SELECT * FROM " + tableDB);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (jsonToLocation(new JSONObject(resultSet.getString("location"))) != null) {
                    NPCCreator npcCreator = new NPCCreator(
                            resultSet.getString("npc_name"),
                            jsonToLocation(new JSONObject(resultSet.getString("location"))),
                            resultSet.getString("npc_skin_name"),
                            resultSet.getString("npc_skin_texture"),
                            resultSet.getString("npc_skin_signature"));

                    npcCreator.setId(resultSet.getInt("id"));
                    npcCreator.setDisappearNPCRange(resultSet.getInt("disappear_range"));
                    npcCreator.setLinkedHologram(resultSet.getString("linked_hologram"));
                    npcCreator.setLinkedHologramHeight(resultSet.getDouble("holo_linked_height"));
                    npcCreator.setEquipment(jsonToPairs(new JSONObject(resultSet.getString("stuff"))));
                    EffectNPC effectNPC = jsonToEffectNPC(new JSONObject(resultSet.getString("effects")));
                    npcCreator.getEffects().setLookLock(effectNPC.getLookLock());
                    npcCreator.getEffects().setCollides(effectNPC.getCollides());
                    NPCsCreator.put(Integer.parseInt(resultSet.getString("id")), npcCreator);
                } else
                    ConsoleLog.error("NPC (" + resultSet.getInt("id") + ") cannot spawn, the location is null or world is null");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return NPCsCreator;
    }

    public static void createNewNPC(NPCCreator npcCreator, Consumer<? super Integer> success) {
        try {
            PreparedStatement insert = LiteFP.getConnection().prepareStatement("INSERT INTO " + tableDB + " (" +
                    "npc_name," +
                    "npc_skin_name," +
                    "disappear_range," +
                    "linked_hologram," +
                    "holo_linked_height," +
                    "npc_skin_texture," +
                    "npc_skin_signature," +
                    "location," +
                    "stuff," +
                    "effects)" +
                    "values (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            insert.setString(1, npcCreator.getName());
            insert.setString(2, npcCreator.getSkinName());
            insert.setInt(3, npcCreator.getDisappearNPCRange());
            insert.setString(4, npcCreator.getLinkedHologram() == null ? null : npcCreator.getLinkedHologram().getName());
            insert.setDouble(5, npcCreator.getLinkedHologramHeight());
            insert.setString(6, npcCreator.getTexture());
            insert.setString(7, npcCreator.getSignature());
            insert.setString(8, locationToJson(npcCreator.getLocation()).toString());
            insert.setString(9, pairsToJson(npcCreator.getEquipment()).toString());
            insert.setString(10, effectNPCToJson(npcCreator.getEffects()).toString());

            int i = insert.executeUpdate();
            if (i == 1) {
                ResultSet key = insert.getGeneratedKeys();
                if (key.next()) {
                    int id = key.getInt(1);
                    success.accept(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeNPC(int id) {
        try {
            PreparedStatement remove = LiteFP.getConnection().prepareStatement("DELETE FROM " + tableDB + " WHERE id=?;");
            remove.setString(1, String.valueOf(id));
            remove.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateNPC(NPCCreator npcCreator) {
        try (PreparedStatement update = LiteFP.getConnection().prepareStatement("UPDATE " + tableDB + " SET " +
                    "npc_name=?," +
                    "npc_skin_name=?," +
                    "disappear_range=?," +
                    "linked_hologram=?," +
                    "holo_linked_height=?," +
                    "npc_skin_texture=?," +
                    "npc_skin_signature=?," +
                    "location=?," +
                    "stuff=?," +
                    "effects=?" +
                    " WHERE id=?")) {
            update.setString(1, npcCreator.getName());
            update.setString(2, npcCreator.getSkinName());
            update.setInt(3, npcCreator.getDisappearNPCRange());
            update.setString(4, npcCreator.getLinkedHologram() == null ? null : npcCreator.getLinkedHologram().getName());
            update.setDouble(5, npcCreator.getLinkedHologramHeight());
            update.setString(6, npcCreator.getTexture());
            update.setString(7, npcCreator.getSignature());
            update.setString(8, locationToJson(npcCreator.getLocation()).toString());
            update.setString(9, pairsToJson(npcCreator.getEquipment()).toString());
            update.setString(10, effectNPCToJson(npcCreator.getEffects()).toString());
            update.setString(11, String.valueOf(npcCreator.getId()));

            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean existNPC(int id) {
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement("SELECT id FROM " + tableDB + " WHERE id=?");
            statement.setString(1, String.valueOf(id));
            return statement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static EffectNPC jsonToEffectNPC(JSONObject jsonObject) {
        EffectNPC effectNPC = new EffectNPC(
                jsonObject.getBoolean("lookLock"),
                jsonObject.getBoolean("collides"));
        return effectNPC;
    }

    private static JSONObject effectNPCToJson(EffectNPC effectNPC) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lookLock", effectNPC.getLookLock());
        jsonObject.put("collides", effectNPC.getCollides());
        return jsonObject;
    }

    private static JSONObject pairsToJson(List<List<Pair<EquipmentSlot, ItemStack>>> stuff) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < stuff.size(); i++) {
            JSONObject json = new JSONObject();
            for (int i1 = 0; i1 < stuff.get(i).size(); i1++) {
                json.put("first", stuff.get(i).get(i1).getFirst().name());
                JSONObject jsonItem = new JSONObject();
                JSONObject jsonEnchant = new JSONObject();
                org.bukkit.inventory.ItemStack itemStack = CraftItemStack.asBukkitCopy(stuff.get(i).get(i1).getSecond());
                jsonItem.put("type", itemStack.getType().name());
                jsonItem.put("amount", itemStack.getAmount());
                if (!itemStack.getEnchantments().isEmpty())
                    for (Enchantment enchantment : itemStack.getEnchantments().keySet())
                        jsonEnchant.put(enchantment.getName(), itemStack.getEnchantments().get(enchantment));

                jsonItem.put("enchantment", jsonEnchant);
                json.put("second", jsonItem);
            }
            jsonObject.put(String.valueOf(i), json);
        }
        return jsonObject;
    }

    private static List<List<Pair<EquipmentSlot, ItemStack>>> jsonToPairs(JSONObject jsonObject) {
        List<List<Pair<EquipmentSlot, ItemStack>>> result = new ArrayList<>();
        for (int i = 0; i < jsonObject.length(); i++) {
            JSONObject json = jsonObject.getJSONObject(String.valueOf(i));
            EquipmentSlot slot = EquipmentSlot.valueOf(json.getString("first"));
            JSONObject itemJson = json.getJSONObject("second");
            org.bukkit.inventory.ItemStack itemStack = ItemBuilder.create(Material.valueOf(itemJson.getString("type")), itemJson.getInt("amount"), null);
            JSONObject enchantmentsJson = itemJson.optJSONObject("enchantment");
            if (enchantmentsJson != null && !enchantmentsJson.isEmpty()) {
                for (String enchantmentName : enchantmentsJson.keySet()) {
                    int level = enchantmentsJson.getInt(enchantmentName);
                    itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantmentName), level);
                }
            }
            result.add(List.of(Pair.of(slot, CraftItemStack.asNMSCopy(itemStack))));
        }
        return result;
    }

    private static JSONObject locationToJson(Location location) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("world", location.getWorld().getName());
        jsonObject.put("x", location.getX());
        jsonObject.put("y", location.getY());
        jsonObject.put("z", location.getZ());
        jsonObject.put("yaw", location.getYaw());
        jsonObject.put("pitch", location.getPitch());
        return jsonObject;
    }

    private static Location jsonToLocation(JSONObject jsonObject) {
        if (Bukkit.getWorld(jsonObject.getString("world")) == null)
            return null;
        return new Location(Bukkit.getWorld(jsonObject.getString("world")), jsonObject.getDouble("x"), jsonObject.getDouble("y"), jsonObject.getDouble("z"), jsonObject.getFloat("yaw"), jsonObject.getFloat("pitch"));
    }

}
