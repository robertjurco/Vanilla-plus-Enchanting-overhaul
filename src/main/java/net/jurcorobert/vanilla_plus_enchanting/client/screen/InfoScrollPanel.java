package net.jurcorobert.vanilla_plus_enchanting.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class InfoScrollPanel extends ScrollPanel {

    // Icon and icon background
    private static final int ICON_SIZE = 16;
    private static final int ICON_BG_SIZE = 18;

    // Slot background
    private static final int SLOT_BG_TOP = 2;
    private static final int SLOT_BG_BOTTOM = 2;
    private static final int SLOT_BG_MIDDLE = 20;
    private static final int SLOT_BG_WIDTH = 152;


    // Slot positioning in scroll area, not internal slot paddings
    private static final int SLOT_PADDING_LEFT = 2;
    private static final int SLOT_PADDING_TOP = 2;

    // Icon layout
    private static final int ICON_OFFSET_X = 3;   // relative to slot padding
    private static final int ICON_OFFSET_Y = 3;

    // Text layout
    private static final int TEXT_OFFSET_X = ICON_BG_SIZE + 6;
    private static final int TEXT_OFFSET_Y = 5;

    private final Font font;

    // private EnchantingTableState clientState;

    // ---- Icons ---- //
    private static final Identifier BOOK_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/item/enchanted_book.png");
    private static final Identifier BOOKSHELF_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/block/bookshelf.png");
    private static final Identifier EXP_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/experience.png");
    private static final Identifier EXP_BLUE_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/experience_blue.png");
    private static final Identifier REDSTONE_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/item/redstone.png");
    private static final Identifier GLOWSTONE_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/item/glowstone_dust.png");
    private static final Identifier GUNPOWDER_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/item/gunpowder.png");
    private static final Identifier SUGAR_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/item/sugar.png");
    private static final Identifier DIAMOND_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/item/diamond_dust.png");
    private static final Identifier AMETHYST_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/item/amethyst_powder.png");
    private static final Identifier ECHO_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/item/echo_powder.png");
    private static final Identifier ENCHANT_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/item/enchanting_powder.png");
    private static final Identifier NETHERITE_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/item/netherite_powder.png");

    private static final Identifier SLOT_BG = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/info_slot.png");



    public InfoScrollPanel(Minecraft minecraft, int width, int height, int left, int top, Font font) {
        super(minecraft, width, height, top, left);

        this.font = font;
    }


    private List<InfoSlot> buildInfoSlots() {
        List<InfoSlot> slots = new ArrayList<>();
        slots.add(new InfoSlot(null, Component.literal("Waiting for enchanting data..."), false));
        return slots;
    }

    @Override
    protected int getContentHeight() {
        int height = SLOT_PADDING_TOP; // initial top padding for scroll

        for (InfoSlot slot : buildInfoSlots()) {
            // split text into lines
            var lines = font.split(slot.text, width - TEXT_OFFSET_X - 6);
            int textHeight = lines.size() * font.lineHeight;

            // total entry height = max of text or icon (including their offsets)
            int entryHeight = Math.max(textHeight + TEXT_OFFSET_Y * 2,
                    ICON_BG_SIZE + ICON_OFFSET_Y * 2);

            height += entryHeight + SLOT_PADDING_TOP; // add spacing between slots
        }

        return height;
    }

    @Override
    protected void drawPanel(GuiGraphics gui, int relativeX, int relativeY, int mouseX, int mouseY) {
        int yOffset = SLOT_PADDING_TOP; // initial top padding

        for (InfoSlot slot : buildInfoSlots()) {
            int slotX = left + SLOT_PADDING_LEFT;
            int slotY = relativeY + yOffset;

            // split text into lines
            var lines = font.split(slot.text, width - TEXT_OFFSET_X - 8);
            int textHeight = lines.size() * font.lineHeight;

            // icon height with offsets
            int iconTotalHeight = ICON_BG_SIZE + ICON_OFFSET_Y * 2;
            int textTotalHeight = textHeight + TEXT_OFFSET_Y * 2;

            // entry height = bigger of icon or text
            int entryHeight = Math.max(iconTotalHeight, textTotalHeight);

            // draw slot background
            drawSlotBackground(gui, slotX, slotY, SLOT_BG_WIDTH, entryHeight);

            // vertical centering
            int iconY = slotY + (entryHeight - ICON_BG_SIZE) / 2;   // icon is centered in entry
            int textY = slotY + (entryHeight - textHeight) / 2;     // text is centered in entry

            // draw icon
            if (slot.icon != null) {
                int iconX = slotX + ICON_OFFSET_X;

                int innerOffset = (ICON_BG_SIZE - ICON_SIZE) / 2;
                gui.blit(slot.icon, iconX + innerOffset, iconY + innerOffset, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            // draw text
            int textX = slotX + TEXT_OFFSET_X;

            for (var line : lines) {
                gui.drawString(font, line, textX, textY, slot.isError ? 0xFF5555 : 0x404040, false);
                textY += font.lineHeight;
            }

            // increment yOffset for next slot
            yOffset += entryHeight + SLOT_PADDING_TOP;
        }
    }

    private void drawSlotBackground(GuiGraphics gui, int x, int y, int width, int height) {
        int middleHeight = height - SLOT_BG_TOP - SLOT_BG_BOTTOM;

        // top
        gui.blit(SLOT_BG, x, y, 0, 0, width, SLOT_BG_TOP, width, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);

        // middle (tiled)
        int drawn = 0;
        while (drawn < middleHeight) {
            int h = Math.min(SLOT_BG_MIDDLE, middleHeight - drawn);
            gui.blit(SLOT_BG, x, y + SLOT_BG_TOP + drawn, 0, SLOT_BG_TOP, width, h, width, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);
            drawn += h;
        }

        // bottom
        gui.blit(SLOT_BG, x, y + height - SLOT_BG_BOTTOM, 0, SLOT_BG_TOP + SLOT_BG_MIDDLE, width, SLOT_BG_BOTTOM, width, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);
    }


    @Override
    public @NonNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {}

    public static class InfoSlot {
        private final Identifier icon;
        private final Component text;
        private final boolean isError;

        public InfoSlot(Identifier icon, Component text, boolean isError) {
            this.icon = icon;
            this.text = text;
            this.isError = isError;
        }
    }
}

