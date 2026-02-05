package net.jurcorobert.vanilla_plus_enchanting.common.registry;

import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModConstants.MOD_ID);

    public static final DeferredItem<Item> DIAMOND_DUST = ITEMS.registerSimpleItem("diamond_dust");
    public static final DeferredItem<Item> ENCHANTING_POWDER = ITEMS.registerSimpleItem("enchanting_powder");
    public static final DeferredItem<Item> ECHO_POWDER = ITEMS.registerSimpleItem("echo_powder");
    public static final DeferredItem<Item> AMETHYST_POWDER = ITEMS.registerSimpleItem("amethyst_powder");
    public static final DeferredItem<Item> NETHERITE_POWDER = ITEMS.registerSimpleItem("netherite_powder");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
