package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class EnchantingMenuManager {
    // Stores all tables by world + position
    private static final Map<Level, Map<BlockPos, EnchantingMenuData>> TABLES = new HashMap<>();

    // Get the table data for a level & position, creating if missing
    public static EnchantingMenuData get(Level level, BlockPos pos) {
        Map<BlockPos, EnchantingMenuData> worldTables = TABLES.computeIfAbsent(level, l -> new HashMap<>());

        // If it already exists, return it
        if (worldTables.containsKey(pos)) return worldTables.get(pos);

        // Only allow server-level (needed for EnchantingTableData)
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("EnchantingTableData only exists on server level!");
        }

        // Otherwise, create new
        EnchantingMenuData data = new EnchantingMenuData(serverLevel, pos);
        worldTables.put(pos, data);
        return data;
    }

    // Optional: remove a table when broken or unloaded
    public static void remove(Level level, BlockPos pos) {
        Map<BlockPos, EnchantingMenuData> worldTables = TABLES.get(level);
        if (worldTables != null) {
            worldTables.remove(pos);
        }
    }
}
