package net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.Config;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class EnchantingPowerManager {
    private static final Map<Identifier, int[]> ENCHANTING_POWER = new HashMap<>();
    private static final Map<Identifier, Integer> ENCHANTING_POWER_BOOK = new HashMap<>();

    // ####################################################### LOADERS #################################################

    public static void load() {
        loadItemPowers();
        loadBookPowers();

    }

    private static void loadItemPowers() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
                        EnchantingPowerManager.class.getResourceAsStream("/data/vanilla_plus_enchanting/enchanting_power/enchanting_power.json")
        ))) {

            Type mapType = new TypeToken<Map<String, int[]>>() {
            }.getType();
            Map<String, int[]> jsonMap = new Gson().fromJson(reader, mapType);

            for (Map.Entry<String, int[]> entry : jsonMap.entrySet()) {
                Identifier id = Identifier.tryParse(entry.getKey());
                if (id != null && entry.getValue().length == 2) {
                    ENCHANTING_POWER.put(id, entry.getValue());
                }
            }

        } catch (Exception e) {
            ModConstants.LOGGER.error("Failed to load enchanting_power.json");
            ModConstants.LOGGER.info(String.valueOf(e));
        }
    }

    private static void loadBookPowers() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
                        EnchantingPowerManager.class.getResourceAsStream("/data/vanilla_plus_enchanting/enchanting_power/enchanting_power_book.json")
        ))) {

            Type mapType = new TypeToken<Map<String, Integer>>() {
            }.getType();
            Map<String, Integer> jsonMap = new Gson().fromJson(reader, mapType);

            for (Map.Entry<String, Integer> entry : jsonMap.entrySet()) {
                Identifier id = Identifier.tryParse(entry.getKey());
                if (id != null && entry.getValue() != null) {
                    ENCHANTING_POWER_BOOK.put(id, entry.getValue());

                    ModConstants.LOGGER.info("Loaded book power: {} = {}", id, entry.getValue());
                }
            }

        } catch (Exception e) {
            ModConstants.LOGGER.error("Failed to load book_power.json");
            ModConstants.LOGGER.info(String.valueOf(e));
        }
    }


    // ####################################################### HELPERS #################################################

    @Nullable
    private static Identifier getEnchantmentId(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(null);
    }


    // ####################################################### CALCULATIONS #################################################

    public static int calculateBaseBookPower(ItemStack stack) {
        Map<Identifier, Integer> enchants = getBookEnchantments(stack);
        if (enchants.isEmpty()) return 0;

        int totalPower = 0;

        for (Map.Entry<Identifier, Integer> entry : enchants.entrySet()) {
            Integer basePower = ENCHANTING_POWER_BOOK.get(entry.getKey());

            // fallback
            if (basePower == null) {
                ModConstants.LOGGER.error("No book power found for {}", entry.getKey());
                continue;
            }

            int level = entry.getValue();
            totalPower += getEnchPowerScaledByLevel(basePower, level);
        }

        return totalPower;
    }

    private static int  calculateCreativeTabItemPower(ItemStack stack) {
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);

        int[] range = ENCHANTING_POWER.get(id);
        if (range == null || range.length != 2){
            ModConstants.LOGGER.error("No item power found for {}", id);
            return -1;
        }

        int min = range[0];
        int max = range[1];

        double mean = (max + min) / 2.0;

        return (int) Math.floor(mean);
    }


    // ####################################################### Getters #################################################

    public static int getEnchPowerCreative(ItemStack stack){
        if (stack.is(Items.ENCHANTED_BOOK)) return calculateBaseBookPower(stack);

        if (stack.isDamageableItem()) return calculateCreativeTabItemPower(stack);

        return -1;
    }

    public static int getBookEnchPowerTrade(ItemStack book){
        int basePower = EnchantingPowerManager.calculateBaseBookPower(book);
        double tradeMultiplier = Config.ENCHANTED_TRADE_MULTIPLIER.get();
        return (int) ((2 - tradeMultiplier) * basePower);
    }

    public static int calculateEnchantedItemPower(ItemStack stack) {
        Random random = new Random();

        // Only applies to damageable items
        if (!stack.isDamageableItem()) {
            return -1;
        }

        // Base crafted power (random, JSON-driven)
        int basePower = getRandomPowerCrafted(stack, random);
        if (basePower < 0) {
            return -1;
        }

        // Subtract enchantment power
        int enchantPower = 0;

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null && !enchantments.isEmpty()) {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                enchantPower += getEnchantPower(entry.getKey(), entry.getIntValue());
            }
        }

        // Final value (clamped)
        return Math.max(0, basePower - enchantPower);
    }

    public static int getRandomPowerCrafted(ItemStack stack, Random random) {
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);

        int[] range = ENCHANTING_POWER.get(id);
        if (range == null || range.length != 2){
            ModConstants.LOGGER.error("No item power found for {}", id);
            return -1;
        }

        int min = range[0];
        int max = range[1];

        double mean = (max + min) / 2.0;
        double stddev = (max - min) / 5.0;

        int value;
        do {
            value = (int) Math.round(random.nextGaussian() * stddev + mean);
        } while (value < min || value > max);

        return value;
    }

    public static int getRandomPowerCrafted(ItemStack stack, int seed) {
        Random random = new Random(seed);
        return getRandomPowerCrafted(stack, random);
    }

    public static int getRandomPowerCrafted(ItemStack stack) {
        Random random = new Random();
        return getRandomPowerCrafted(stack, random);
    }

    public static int getEnchPowerScaledByLevel(int basePower, int level){
        float levelMul = 1.0f + (float) Math.sqrt(level - 1) * 0.6f;
        return Math.round(basePower * levelMul);
    }

    public static Map<Identifier, Integer> getBookEnchantments(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return Map.of();

        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) return Map.of();

        Map<Identifier, Integer> result = new HashMap<>();

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Identifier id = getEnchantmentId(entry.getKey());
            if (id != null) {
                result.put(id, entry.getIntValue());
            }
        }

        return result;
    }

    public static int getEnchantPower(Holder<Enchantment> holder, int level) {
        Identifier id = getEnchantmentId(holder);
        if (id == null) return 1; // hard fallback

        Integer basePower = ENCHANTING_POWER_BOOK.get(id);
        if (basePower == null) return 1;

        return getEnchPowerScaledByLevel(basePower, level);
    }
}
