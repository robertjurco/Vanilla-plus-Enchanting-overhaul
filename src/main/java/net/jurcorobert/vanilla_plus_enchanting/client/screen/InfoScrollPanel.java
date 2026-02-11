package net.jurcorobert.vanilla_plus_enchanting.client.screen;

import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenu;
import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenuState;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private static final int INITIAL_SLOT_PADDING_TOP = -2;

    // Icon layout
    private static final int ICON_OFFSET_X = 3;   // relative to slot padding
    private static final int ICON_OFFSET_Y = 3;

    // Text layout
    private static final int TEXT_OFFSET_X = ICON_BG_SIZE + 6;
    private static final int TEXT_OFFSET_Y = 5;

    private static final int ITEM_SLOT = 36;
    private static final int BOOK_SLOT = 37;
    private static final int DUST_SLOT_1 = 38;
    private static final int DUST_SLOT_2 = 39;
    private static final int DUST_SLOT_3 = 40;
    private static final int DYE_SLOT = 41;

    private final Font font;
    private final EnchantingMenu menu;

    // Menu state
    private EnchantingMenuState clientState;

    private List<InfoSlot> infoSlots = new ArrayList<>(); // cache slots

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
    private static final Identifier SLOT_BG_SCROLLBAR_OPEN = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/info_slot_scrollbar_open.png");



    public InfoScrollPanel(Minecraft minecraft, int width, int height, int left, int top, Font font, EnchantingMenu menu) {
        super(minecraft, width, height, top, left);

        this.font = font;
        this.menu = menu;
    }

    public void refresh(EnchantingMenuState state) {
        this.clientState = state;

        // rebuild slots only once per refresh
        this.infoSlots = buildInfoSlots();
    }


    private List<InfoSlot> buildInfoSlots() {
        ModConstants.LOGGER.info("buildInfoSlots");
        List<InfoSlot> slots = new ArrayList<>();

        if (clientState == null) {
            slots.add(new InfoSlot(null, Component.literal("Waiting for enchanting data..."), false));
            return slots;
        }

        // Bookshelf

        // Experience

        // Power

        // Applicable enchantments
        if (clientState.getMode() == 0 &&
                !clientState.hasApplicableEnchantments() &&
                !menu.getSlot(ITEM_SLOT).getItem().isEmpty() &&
                !menu.getSlot(BOOK_SLOT).getItem().isEmpty() &&
                menu.getSlot(ITEM_SLOT).getItem().isDamageableItem() &&
                menu.getSlot(BOOK_SLOT).getItem().is(Items.ENCHANTED_BOOK))
            slots.add(new InfoSlot(BOOK_ICON, Component.literal("No applicable enchantments"), true));

        if (clientState.getMode() == 1 &&
                !clientState.hasNonCurseEnchantments() &&
                clientState.getCursesLocked() &&
                !menu.getSlot(ITEM_SLOT).getItem().isEmpty() &&
                !menu.getSlot(BOOK_SLOT).getItem().isEmpty() &&
                menu.getSlot(ITEM_SLOT).getItem().isDamageableItem() &&
                menu.getSlot(BOOK_SLOT).getItem().is(Items.BOOK))
            slots.add(new InfoSlot(BOOK_ICON, Component.literal("No enchantments to transfer"), true));

        // Mode checks
        if (clientState.hasTooMuchDiamondDust())
            slots.add(new InfoSlot(DIAMOND_ICON, Component.literal("Max one Diamond Dust"), true));
        else {
            if (clientState.getMode() == 0 && (
                    menu.getSlot(ITEM_SLOT).getItem().isEmpty() ||
                    menu.getSlot(BOOK_SLOT).getItem().isEmpty() ||
                    !menu.getSlot(ITEM_SLOT).getItem().isDamageableItem() ||
                    !menu.getSlot(BOOK_SLOT).getItem().is(Items.ENCHANTED_BOOK)))
                slots.add(new InfoSlot(DIAMOND_ICON, Component.literal("Enchanting requires item and enchanted book"), true));
            else if (clientState.getMode() == 0)
                slots.add(new InfoSlot(DIAMOND_ICON, Component.literal("Enchanting mode"), false));

            if (clientState.getMode() == 1 && (
                    menu.getSlot(ITEM_SLOT).getItem().isEmpty() ||
                    menu.getSlot(BOOK_SLOT).getItem().isEmpty() ||
                    !menu.getSlot(ITEM_SLOT).getItem().isDamageableItem() ||
                    !menu.getSlot(BOOK_SLOT).getItem().is(Items.BOOK)))
                slots.add(new InfoSlot(DIAMOND_ICON, Component.literal("Disenchanting requires enchanted item and plain book"), true));
            else if (clientState.getMode() == 1)
                slots.add(new InfoSlot(DIAMOND_ICON, Component.literal("Disenchanting mode"), false));
        }

        // Tool break chance
        if (clientState.hasTooMuchGlowstoneDust()) {
            slots.add(new InfoSlot(GLOWSTONE_ICON, Component.literal("Max one Glowstone Dust"), true));
        } else {
            slots.add(new InfoSlot(GLOWSTONE_ICON, Component.literal("Tool break chance: " + Math.round(clientState.getToolBreakChance() * 100) + "%"), false));
        }

        // Fail chance
        slots.add(new InfoSlot(REDSTONE_ICON, Component.literal("Fail chance: " + Math.round(clientState.getFailChance() * 100) + "%"), false));

        // Enchanting cost
        if (clientState.getReducePowerMultiplier() < 1 && clientState.getMode() == 0)
            slots.add(new InfoSlot(ENCHANT_ICON, Component.literal("Enchanting power cost is decreased by: " + Math.round(100 - clientState.getReducePowerMultiplier() * 100) + "%"), false));
        if (clientState.getReducePowerMultiplier() < 1 && clientState.getMode() == 1)
            slots.add(new InfoSlot(ENCHANT_ICON, Component.literal("Enchanting powder is not usable while disenchanting."), true));


        // Curse prevention
        if (clientState.hasTooMuchSugar())
            slots.add(new InfoSlot(SUGAR_ICON, Component.literal("Max one Sugar"), true));
        else if (clientState.getCursesLocked())
            slots.add(new InfoSlot(SUGAR_ICON, Component.literal("Prevents curses from appearing"), false));

        // Enchantment locking
        if (clientState.hasTooMuchAmethystDust())
            slots.add(new InfoSlot(AMETHYST_ICON, Component.literal("Max one Amethyst dust"), true));
        else if (clientState.getExistingEnchantsLocked() && clientState.getMode() == 1)
            slots.add(new InfoSlot(AMETHYST_ICON, Component.literal("Amethyst powder is usable only while enchanting."), true));
        else if (clientState.getExistingEnchantsLocked() && clientState.getMode()    == 0)
            slots.add(new InfoSlot(AMETHYST_ICON, Component.literal("Prevents replacing enchantments that are already on the item."), false));

        // Disenchanting efficiency
        if (clientState.getDisenchantEfficiency() < 1 && clientState.getMode() == 0)
            slots.add(new InfoSlot(ECHO_ICON, Component.literal("Echo powder is usable only while disenchanting."), true));
        if (clientState.getDisenchantEfficiency() < 1 && clientState.getMode() == 1)
            slots.add(new InfoSlot(ECHO_ICON, Component.literal("Disenchanting will decrease the cost of the book by " + Math.round(100 - clientState.getDisenchantEfficiency() * 100) + "%"), false));

        // Extra enchants

        // Upgrade chance

        return slots;
    }

    @Override
    protected int getContentHeight() {
        int height = INITIAL_SLOT_PADDING_TOP; // initial top padding for scroll

        for (InfoSlot slot : infoSlots) {
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

    private boolean isScrollbarVisible() {
        return getContentHeight() > height;
    }

    private int getScrollbarWidth() {
        return 6; // adjust depending on your scrollbar texture/size
    }

    @Override
    protected void drawPanel(GuiGraphics gui, int relativeX, int relativeY, int mouseX, int mouseY) {
        int yOffset = INITIAL_SLOT_PADDING_TOP; // initial top padding
        int availableWidth = width - (isScrollbarVisible() ? getScrollbarWidth() : 0);

        for (InfoSlot slot : infoSlots) {
            int slotX = left + SLOT_PADDING_LEFT;
            int slotY = relativeY + yOffset;

            // available width
            int slotWidth = Math.min(SLOT_BG_WIDTH, availableWidth);

            // split text into lines
            var lines = font.split(slot.text, slotWidth - TEXT_OFFSET_X - 8);
            int textHeight = lines.size() * font.lineHeight;

            // icon height with offsets
            int iconTotalHeight = ICON_BG_SIZE + ICON_OFFSET_Y * 2;
            int textTotalHeight = textHeight + TEXT_OFFSET_Y * 2;

            // entry height = bigger of icon or text
            int entryHeight = Math.max(iconTotalHeight, textTotalHeight);

            // draw slot background
            drawSlotBackground(gui, slotX, slotY, slotWidth, entryHeight);

            // vertical centering
            int iconY = slotY + (entryHeight - ICON_BG_SIZE) / 2;   // icon is centered in entry
            int textY = slotY + (entryHeight - textHeight) / 2;     // text is centered in entry

            // draw icon
            if (slot.icon != null) {
                int iconX = slotX + ICON_OFFSET_X;

                int innerOffset = (ICON_BG_SIZE - ICON_SIZE) / 2;
                gui.blit(RenderPipelines.GUI_TEXTURED, slot.icon, iconX + innerOffset, iconY + innerOffset, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }

            // draw text
            int textX = slotX + TEXT_OFFSET_X;

            for (var line : lines) {
                gui.drawString(font, line, textX, textY, slot.isError ? -33152 : -12566464, false);
                textY += font.lineHeight;
            }

            // increment yOffset for next slot
            yOffset += entryHeight + SLOT_PADDING_TOP;
        }
    }

    private void drawSlotBackground(GuiGraphics gui, int x, int y, int width, int height) {

        Identifier tex = isScrollbarVisible() ? SLOT_BG_SCROLLBAR_OPEN : SLOT_BG;

        int middleHeight = height - SLOT_BG_TOP - SLOT_BG_BOTTOM;

        // top
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, width, SLOT_BG_TOP, SLOT_BG_WIDTH, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);

        // middle (tiled)
        int drawn = 0;
        while (drawn < middleHeight) {
            int h = Math.min(SLOT_BG_MIDDLE, middleHeight - drawn);
            gui.blit(RenderPipelines.GUI_TEXTURED, tex, x, y + SLOT_BG_TOP + drawn, 0, SLOT_BG_TOP, width, h, SLOT_BG_WIDTH, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);
            drawn += h;
        }

        // bottom
        gui.blit(RenderPipelines.GUI_TEXTURED, tex, x, y + height - SLOT_BG_BOTTOM, 0, SLOT_BG_TOP + SLOT_BG_MIDDLE, width, SLOT_BG_BOTTOM, SLOT_BG_WIDTH, SLOT_BG_TOP + SLOT_BG_MIDDLE + SLOT_BG_BOTTOM);
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

