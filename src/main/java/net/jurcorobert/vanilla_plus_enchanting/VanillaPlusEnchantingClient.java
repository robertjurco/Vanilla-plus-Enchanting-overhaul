package net.jurcorobert.vanilla_plus_enchanting;

import net.jurcorobert.vanilla_plus_enchanting.client.ModTooltip;
import net.jurcorobert.vanilla_plus_enchanting.client.screen.EnchantingScreen;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModMenus;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ModConstants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public class VanillaPlusEnchantingClient {
    public VanillaPlusEnchantingClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ModTooltip::onTooltip);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ENCHANTING_MENU.get(), EnchantingScreen::new);
    }
}
