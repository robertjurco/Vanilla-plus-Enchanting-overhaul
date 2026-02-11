package net.jurcorobert.vanilla_plus_enchanting.common.villager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.jurcorobert.vanilla_plus_enchanting.Config;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.common.utils.EnchantmentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Handles custom item enchantments using JSON-defined pools.
 */
public class EnchantedItemTradePool {

    /** category -> list of enchantments with min/max levels */
    private static final Map<String, List<EnchantmentEntry>> ENCHANTED_ITEM_TRADE_POOL = new HashMap<>();

    /** item -> category mapping (example: Items.IRON_SWORD -> "iron") */
    private static final Map<Item, String> ITEM_CATEGORIES = new HashMap<>();

    static {
        // Example categories, can be expanded or loaded from JSON as well
        ITEM_CATEGORIES.put(Items.IRON_SWORD, "iron");
        ITEM_CATEGORIES.put(Items.IRON_AXE, "iron");
        ITEM_CATEGORIES.put(Items.IRON_SHOVEL, "iron");
        ITEM_CATEGORIES.put(Items.IRON_PICKAXE, "iron");

        ITEM_CATEGORIES.put(Items.DIAMOND_AXE, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_SWORD, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_SHOVEL, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_PICKAXE, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_LEGGINGS, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_BOOTS, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_HELMET, "diamond");
        ITEM_CATEGORIES.put(Items.DIAMOND_CHESTPLATE, "diamond");

        ITEM_CATEGORIES.put(Items.BOW, "other");
        ITEM_CATEGORIES.put(Items.CROSSBOW, "other");
        ITEM_CATEGORIES.put(Items.FISHING_ROD, "other");
    }

    /** Load the enchantment pools from JSON */
    public static void loadPool() {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(EnchantedItemTradePool.class.getResourceAsStream(
                        "/data/vanilla_plus_enchanting/trade/enchanted_item_trade_pool.json")))) {

            Type mapType = new TypeToken<Map<String, List<EnchantmentEntry>>>() {}.getType();
            Map<String, List<EnchantmentEntry>> jsonMap = new Gson().fromJson(reader, mapType);

            for (Map.Entry<String, List<EnchantmentEntry>> entry : jsonMap.entrySet()) {
                if (entry.getValue() != null) {
                    ENCHANTED_ITEM_TRADE_POOL.put(entry.getKey(), entry.getValue());
                    System.out.println("Loaded pool " + entry.getKey() + " with " + entry.getValue().size() + " entries");
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load enchanted_item_trade_pool.json");
            e.printStackTrace();
        }
    }

    public static void applyRandomEnchants(ItemStack stack, ServerLevel level, RandomSource random) {
        String category = ITEM_CATEGORIES.get(stack.getItem());
        if (category == null) return; // no category, nothing to apply

        // get starting power
        int power = EnchantingPowerManager.getRandomPowerCrafted(stack);

        // get enchantment pool
        List<EnchantmentEntry> pool = ENCHANTED_ITEM_TRADE_POOL.get(category);
        if (pool == null || pool.isEmpty()) return;

        // Filter out enchantments that cannot apply to this item
        List<EnchantmentEntry> applicable = filterApplicableEnchantments(stack, level, pool);
        if (applicable.isEmpty()) return;

        // Decide how many enchantments to apply (1–3)
        int enchantCount = 1 + random.nextInt(Math.min(3, applicable.size()));

        // Shuffle applicable list to avoid duplicates
        Collections.shuffle(applicable, new Random(random.nextLong()));

        // Apply random set of enchantments
        power = applyRandomEnchantments(stack, power, level, applicable, enchantCount);

        // decrease power for traded item
        if (power < 0) power = 0;
        double tradeMultiplier = Config.ENCHANTED_TRADE_MULTIPLIER.get();
        power = (int) (tradeMultiplier * power);
        EnchantingPower.set(stack, power);

    }

    public static List<EnchantedItemTradePool.EnchantmentEntry> filterApplicableEnchantments(ItemStack stack, ServerLevel level, List<EnchantedItemTradePool.EnchantmentEntry> pool) {
        List<EnchantmentEntry> applicable = new ArrayList<>();

        // Loop through pool
        for (EnchantmentEntry entry : pool) {
            Holder<Enchantment> holder = EnchantmentHelper.getEnchantmentHolder(level.registryAccess(), entry.id);

            Enchantment enchant = holder.value();

            HolderSet<Item> supported = enchant.definition().supportedItems();
            if (!stack.is(supported)) continue;

            applicable.add(entry);
        }

        return applicable;
    }

    public static int applyRandomEnchantments(ItemStack stack, int power, ServerLevel level, List<EnchantedItemTradePool.EnchantmentEntry> pool, int enchantCount) {
        Random random = new Random();

        List<EnchantmentEntry> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);

        Set<Holder<Enchantment>> applied = new HashSet<>();

        int powerRemaining = power;

        for (EnchantmentEntry entry : copy) {
            if (applied.size() >= enchantCount) break;

            Holder<Enchantment> holder = EnchantmentHelper.getEnchantmentHolder(level.registryAccess(), entry.id);
            if (holder == null) continue;

            if (applied.contains(holder)) continue;

            // Skip if already applied or incompatible with applied enchants
            boolean compatible = true;
            for (Holder<Enchantment> existing : applied) {
                if (!Enchantment.areCompatible(existing, holder)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) continue;

            int levelValue = entry.min + random.nextInt(entry.max - entry.min + 1);
            int enchPower = EnchantingPowerManager.getEnchantPower(holder, levelValue);
            if (enchPower > powerRemaining) continue;

            // Apply enchant
            powerRemaining -= enchPower;
            stack.enchant(holder, levelValue);
            applied.add(holder);
        }

        return power;
    }

    /** JSON data class */
    public static class EnchantmentEntry {
        public String id; // e.g., "minecraft:sharpness"
        public int min;
        public int max;
    }
}
