package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.villager.GenerateEnchantedTrade;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerTrades.class)
public class VillagerTradeMixinOnSpawn {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void modifyVillagerTrades(CallbackInfo ci) {

        // List of professions to modify (ResourceKey references)
        List<ResourceKey<VillagerProfession>> professions = List.of(
                VillagerProfession.LIBRARIAN,
                VillagerProfession.ARMORER,
                VillagerProfession.WEAPONSMITH,
                VillagerProfession.TOOLSMITH,
                VillagerProfession.FLETCHER,
                VillagerProfession.FISHERMAN,
                VillagerProfession.LEATHERWORKER
        );

        // Loop through professions
        for (ResourceKey<VillagerProfession> prof : professions) {
            var trades = VillagerTrades.TRADES.get(prof);
            if (trades == null) continue;

            // Level of villager per tier
            for (int tier = 1; tier <= 5; tier++) {
                VillagerTrades.ItemListing[] original = trades.get(tier);
                if (original == null) continue;

                // modify trades
                List<VillagerTrades.ItemListing> modified =modifyTrades(original, tier);

                // save new trades
                trades.put(tier, modified.toArray(new VillagerTrades.ItemListing[0]));
            }
        }
    }

    private static List<VillagerTrades.ItemListing> modifyTrades(VillagerTrades.ItemListing[] original, int tier) {
        List<VillagerTrades.ItemListing> result = new ArrayList<>();

        // loop through trades, modify enchanted book trades
        for (VillagerTrades.ItemListing listing : original) {
            VillagerTrades.ItemListing newListing = GenerateEnchantedTrade.processListing(listing, tier);
            result.add(newListing);
        }

        return result;
    }
}
