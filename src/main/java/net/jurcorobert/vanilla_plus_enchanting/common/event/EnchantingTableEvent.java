package net.jurcorobert.vanilla_plus_enchanting.common.event;

import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class EnchantingTableEvent {

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        // Check if clicked block is enchanting table
        if (event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            final var pos = event.getPos();
            final var player = event.getEntity();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new EnchantingMenu(id, inv, pos),
                        Component.translatable("container.enchant")
                ), buf -> buf.writeBlockPos(pos)); // send block position to client
            }
        }
    }
}
