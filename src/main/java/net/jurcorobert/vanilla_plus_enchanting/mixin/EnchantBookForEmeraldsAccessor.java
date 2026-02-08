package net.jurcorobert.vanilla_plus_enchanting.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.entity.npc.villager.VillagerTrades$EnchantBookForEmeralds")
public interface EnchantBookForEmeraldsAccessor {
    // marker interface — nothing else needed
}