package net.jurcorobert.vanilla_plus_enchanting.common;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GrindstoneEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID)
public class GrindstoneHandler {

    @SubscribeEvent
    public static void onGrindstoneTake(GrindstoneEvent.OnTakeItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();

        int bonusXp = 0;

        if (!top.isEmpty()) {
            bonusXp += EnchantingPowerManager.getEPofEnchantsOnItem(top);
        }
        if (!bottom.isEmpty()) {
            bonusXp += EnchantingPowerManager.getEPofEnchantsOnItem(bottom);
        }

        // Override or modify XP safely
        event.setXp(bonusXp * 4);
    }
}