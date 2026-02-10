package net.jurcorobert.vanilla_plus_enchanting.common.network;

import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantPayloadHandler {

    public static void handle(
            EnchantRequestPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;

            // Validate container
            if (player.containerMenu.containerId != payload.containerId()) {
                return; // spoofed packet
            }

            if (player.containerMenu instanceof EnchantingMenu menu) {
                menu.performEnchant(player);
            }
        });
    }
}