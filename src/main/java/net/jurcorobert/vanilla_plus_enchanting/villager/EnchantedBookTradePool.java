package net.jurcorobert.vanilla_plus_enchanting.villager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.jurcorobert.vanilla_plus_enchanting.utils.EnchantmentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnchantedBookTradePool {

    private static final Map<String, List<EnchantmentEntry>> ENCHANTED_BOOK_TRADE_POOL = new HashMap<>();

    public static void loadPool() {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(
                        EnchantedBookTradePool.class.getResourceAsStream("/data/vanilla_plus_enchanting/trade/enchant_trade_pool.json")))) {

            Type mapType = new TypeToken<Map<String, List<EnchantmentEntry>>>() {}.getType();
            Map<String, List<EnchantmentEntry>> jsonMap = new Gson().fromJson(reader, mapType);

            for (Map.Entry<String, List<EnchantmentEntry>> entry : jsonMap.entrySet()) {
                if (entry.getValue() != null) {
                    ENCHANTED_BOOK_TRADE_POOL.put(entry.getKey(), entry.getValue());
                    ModConstants.LOGGER.info("Loaded pool {} with {} entries", entry.getKey(), entry.getValue().size());
                }
            }

        } catch (Exception e) {
            ModConstants.LOGGER.error("Failed to load enchanting_trades_pool.json");
            e.printStackTrace();
        }
    }

    public static ItemStack getRandomBook(ServerLevel level, int tier, RandomSource random) {
        List<EnchantmentEntry> pool = ENCHANTED_BOOK_TRADE_POOL.get(getTierName(tier));
        if (pool == null || pool.isEmpty()) return ItemStack.EMPTY;

        // pick random enchantment and level
        EnchantmentEntry entry = pool.get(random.nextInt(pool.size()));

        String enchantmentId = entry.id;
        int enchantmentLevel = entry.min + random.nextInt(entry.max - entry.min + 1);

        // Create enchanted book
        RegistryAccess registryAccess = level.registryAccess();

        return EnchantmentHelper.getBookFromEnchantment(registryAccess, enchantmentId, enchantmentLevel);
    }

    public static class EnchantmentEntry {
        public String id; // store as string
        public int min;
        public int max;
    }

    private static String getTierName(int tier) {
        return switch (tier) {
            case 1 -> "novice";
            case 2 -> "apprentice";
            case 3 -> "journeyman";
            case 4 -> "expert";
            case 5 -> "master";
            default -> "novice";
        };
    }
}
