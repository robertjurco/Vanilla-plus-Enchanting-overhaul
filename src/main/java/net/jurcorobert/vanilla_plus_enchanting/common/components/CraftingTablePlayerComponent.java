package net.jurcorobert.vanilla_plus_enchanting.common.components;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class CraftingTablePlayerComponent {

    // WeakHashMap ensures data is garbage collected when player object is gone
    private static final Map<Player, CraftingTablePlayerComponent> PLAYER_DATA = new WeakHashMap<>();

    private int playerCraftingSeed;
    private boolean needsReroll;

    private CraftingTablePlayerComponent() {
        // initialize with random player crafting seed
        this.playerCraftingSeed = new Random().nextInt();
    }

    public static CraftingTablePlayerComponent get(Player player) {
        return PLAYER_DATA.computeIfAbsent(player, p -> new CraftingTablePlayerComponent());
    }

    public int getPlayerCraftingSeed() {
        return playerCraftingSeed;
    }

    public void markForReroll() {
        this.needsReroll = true;
    }

    public void rerollIfNeeded() {
        if (needsReroll) {
            this.playerCraftingSeed = new Random().nextInt();
            this.needsReroll = false;
        }
    }
}