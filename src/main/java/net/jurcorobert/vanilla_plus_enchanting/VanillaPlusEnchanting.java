package net.jurcorobert.vanilla_plus_enchanting;

import com.mojang.serialization.MapCodec;
import net.jurcorobert.vanilla_plus_enchanting.common.loot.EnchantingPowerModifier;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;

import net.jurcorobert.vanilla_plus_enchanting.common.villager.EnchantedBookTradePool;
import net.jurcorobert.vanilla_plus_enchanting.common.villager.EnchantedItemTradePool;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ModConstants.MOD_ID)
public class VanillaPlusEnchanting {
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.



    // Register loot tables
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ModConstants.MOD_ID);

    public static final Supplier<MapCodec<EnchantingPowerModifier>> ENCHANTING_POWER_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("loot_enchanting_power_modifier", () -> EnchantingPowerModifier.CODEC);

    public VanillaPlusEnchanting(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register registries
        ModItems.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our GLM DeferredRegister
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        // Load mod specific data
        EnchantedBookTradePool.loadPool();
        EnchantedItemTradePool.loadPool();

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.DIAMOND_DUST);
            event.accept(ModItems.ENCHANTING_POWDER);
            event.accept(ModItems.AMETHYST_POWDER);
            event.accept(ModItems.ECHO_POWDER);
            event.accept(ModItems.NETHERITE_POWDER);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
