package net.jurcorobert.vanilla_plus_enchanting.mixin;

import net.jurcorobert.vanilla_plus_enchanting.common.villager.GenerateEnchantedTrade;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Villager.class)
public class VillagerTradeMixinOnTrade {

    // List of professions to modify (ResourceKey references)
    private static final Set<ResourceKey<VillagerProfession>> TARGET_PROFESSIONS = Set.of(
            VillagerProfession.LIBRARIAN,
            VillagerProfession.ARMORER,
            VillagerProfession.WEAPONSMITH,
            VillagerProfession.TOOLSMITH,
            VillagerProfession.FLETCHER,
            VillagerProfession.FISHERMAN,
            VillagerProfession.LEATHERWORKER
    );

    @Inject(method = "rewardTradeXp", at = @At("RETURN"))
    private void onDamageableTrade(MerchantOffer offer, CallbackInfo ci) {
        Villager villager = (Villager)(Object)this;

        if (!(villager.level() instanceof ServerLevel serverLevel)) return;

        // --- profession check (NEW API) ---
        Holder<VillagerProfession> profession = villager.getVillagerData().profession();
        if (TARGET_PROFESSIONS.stream().noneMatch(profession::is)) return;

        var offers = villager.getOffers();
        RandomSource random = villager.getRandom();

        int index = offers.indexOf(offer);
        if (index < 0) return;

        MerchantOffer modified = GenerateEnchantedTrade.modifyOffer(offer, serverLevel, villager, random);

        if (modified == offer) return;

        offers.set(index, modified);

        if (villager.getTradingPlayer() != null) {
            villager.getTradingPlayer().sendMerchantOffers(
                    villager.getTradingPlayer().containerMenu.containerId,
                    offers,
                    villager.getVillagerData().level(),
                    villager.getVillagerXp(),
                    villager.showProgressBar(),
                    villager.canRestock()
            );
        }
    }
}