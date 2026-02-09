package net.jurcorobert.vanilla_plus_enchanting.common.villager;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenerateEnchantedTrade {

    private static MerchantOffer getEnchantedBookTrade(MerchantOffer offer, ServerLevel level, Entity villager, RandomSource rand) {
        int tier = ((Villager) villager).getVillagerData().level();

        // Get a random enchanted book
        ItemStack book = EnchantedBookTradePool.getRandomBook(level, tier, rand);
        if (book.isEmpty()) return null;

        // resolve book power
        int power = EnchantingPowerManager.getBookEnchPowerVillagerTrade(book);
        ModConstants.LOGGER.info(String.valueOf(power));
        EnchantingPower.set(book, power);

        // fallback
        ItemEnchantments bookEnchants = book.get(DataComponents.STORED_ENCHANTMENTS);
        if (bookEnchants == null || bookEnchants.isEmpty()) return null;

        // Pick the first enchantment in the book
        Holder<Enchantment> enchantHolder = bookEnchants.keySet().iterator().next();
        int enchantLevel = bookEnchants.getLevel(enchantHolder);

        // Calculate emerald cost like vanilla
        int emeraldCost = 2 + rand.nextInt(5 + enchantLevel * 10) + 3 * enchantLevel;
        if (enchantHolder.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) emeraldCost *= 2;
        if (emeraldCost > 64) emeraldCost = 64;

        return new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                Optional.of(new ItemCost(Items.BOOK, 1)),
                book,
                12,      // max uses
                getBookTradeXp(tier),      // villager XP
                0.05F    // price multiplier
        );
    }

    private static MerchantOffer getEnchantedItemTrade(MerchantOffer offer, ServerLevel level, Entity villager, RandomSource rand) {
        ItemStack result = offer.getResult();

        ItemStack copy = new ItemStack(result.getItem(), result.getCount());

        EnchantedItemBaseValues.EnchantedTradeData data = EnchantedItemBaseValues.VALUES.get(copy.getItem());
        if (data == null) throw new IllegalArgumentException("No EnchantedBaseValues defined for item: " + copy);

        EnchantedItemTradePool.applyRandomEnchants(copy, level, rand);

        int emeraldCost = data.baseEmeraldCost + rand.nextInt(16);
        if (emeraldCost > 64) emeraldCost = 64;

        return new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                copy,
                data.maxUses,
                data.villagerXp,
                data.priceMultiplier
        );
    }

    private static MerchantOffer genNonEnchantedTrade(MerchantOffer offer) {
        ItemStack result = offer.getResult();
        int power = EnchantingPowerManager.getRandomPowerCrafted(result);

        EnchantingPower.set(result, power);

        // Build a new MerchantOffer so client sees it as updated
        ItemStack base = offer.getBaseCostA();
        ItemStack costBStack = offer.getCostB();
        return new MerchantOffer(
                new ItemCost(base.getItem(), base.getCount()),
                costBStack.isEmpty() ? Optional.empty() : Optional.of(new ItemCost(costBStack.getItem(), costBStack.getCount())),
                result,
                offer.getUses(),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                offer.getDemand()
        );
    }

    public static MerchantOffer modifyOffer(MerchantOffer offer, ServerLevel level, Entity villager, RandomSource rand) {
        ItemStack result = offer.getResult();

        // Offer is enchanted book
        if (result.getItem() == Items.ENCHANTED_BOOK)
            return getEnchantedBookTrade(offer, level, villager, rand);

        // Offer is damageable item but not enchanted → apply random power
        if (result.isDamageableItem() && !result.isEnchanted())
            return genNonEnchantedTrade(offer);

        // Already enchanted → replace with custom enchanted trade
        if (result.isEnchanted())
            return getEnchantedItemTrade(offer, level, villager, rand);

        return offer;
    }

    private static VillagerTrades.ItemListing modifyListing(VillagerTrades.ItemListing listing) {
        return (level, villager, rand) -> {
            // Get offer
            MerchantOffer offer = listing.getOffer(level, villager, rand);
            if (offer == null) return null;

            // Modify the offer using the shared method
            return modifyOffer(offer, level, villager, rand);
        };
    }

    public static List<VillagerTrades.ItemListing> modifyListings(VillagerTrades.ItemListing[] original) {
        List<VillagerTrades.ItemListing> result = new ArrayList<>();

        // loop through trades, modify enchanted book trades
        for (VillagerTrades.ItemListing listing : original) {
            VillagerTrades.ItemListing newListing = GenerateEnchantedTrade.modifyListing(listing);
            result.add(newListing);
        }

        return result;
    }

    private static int getBookTradeXp(int tier) {
        return switch (tier) {
            case 1 -> 1;
            case 2 -> 5;
            case 3 -> 10;
            case 4 -> 15;
            case 5 -> 30;
            default -> 1;
        };
    }

    private static class EnchantedItemBaseValues {
        private static final Map<Item, EnchantedTradeData> VALUES = new Object2ObjectOpenHashMap<>();

        static {
            // FLETCHER
            VALUES.put(Items.BOW, new EnchantedTradeData(2, 3, 15, 0.2F));
            VALUES.put(Items.CROSSBOW, new EnchantedTradeData(3, 3, 15, 0.2F));

            // FISHERMAN
            VALUES.put(Items.FISHING_ROD, new EnchantedTradeData(3, 3, 10, 0.2F));

            // WEAPONSMITH
            VALUES.put(Items.IRON_SWORD, new EnchantedTradeData(2, 3, 1, 0f));
            VALUES.put(Items.DIAMOND_SWORD, new EnchantedTradeData(8, 3, 30, 0.2F));

            // ARMORER
            VALUES.put(Items.DIAMOND_LEGGINGS, new EnchantedTradeData(14, 3, 15, 0.2F));
            VALUES.put(Items.DIAMOND_BOOTS, new EnchantedTradeData(8, 3, 15, 0.2F));
            VALUES.put(Items.DIAMOND_HELMET, new EnchantedTradeData(8, 3, 30, 0.2F));
            VALUES.put(Items.DIAMOND_CHESTPLATE, new EnchantedTradeData(16, 3, 30, 0.2F));

            // TOOLSMITH
            VALUES.put(Items.IRON_AXE, new EnchantedTradeData(1, 3, 10, 0.2F));
            VALUES.put(Items.IRON_SHOVEL, new EnchantedTradeData(2, 3, 10, 0.2F));
            VALUES.put(Items.IRON_PICKAXE, new EnchantedTradeData(3, 3, 10, 0.2F));
            VALUES.put(Items.DIAMOND_AXE, new EnchantedTradeData(12, 3, 15, 0.2F)); // same for weaponsmith
            VALUES.put(Items.DIAMOND_SHOVEL, new EnchantedTradeData(5, 3, 15, 0.2F));
            VALUES.put(Items.DIAMOND_PICKAXE, new EnchantedTradeData(13, 3, 30, 0.2F));
        }

        private record EnchantedTradeData(int baseEmeraldCost, int maxUses, int villagerXp, float priceMultiplier) {}
    }

}
