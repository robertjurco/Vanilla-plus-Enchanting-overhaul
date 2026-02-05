package net.jurcorobert.vanilla_plus_enchanting;

import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public class ModSetup {
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        EnchantingPowerManager.load();
    }
}
