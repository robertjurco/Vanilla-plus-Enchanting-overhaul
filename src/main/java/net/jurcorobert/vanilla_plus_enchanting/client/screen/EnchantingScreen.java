package net.jurcorobert.vanilla_plus_enchanting.client.screen;

import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenu;
import net.jurcorobert.vanilla_plus_enchanting.common.menu.EnchantingMenuState;
import net.jurcorobert.vanilla_plus_enchanting.common.network.EnchantRequestPayload;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantingScreen extends AbstractContainerScreen<EnchantingMenu> {

    // ---- Textures ---- //
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/custom_enchanting.png");
    private static final Identifier INFO_PANEL_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/info_panel.png");

    private static final Identifier ARROW_RIGHT_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/arrow_right.png");
    private static final Identifier ARROW_LEFT_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/arrow_left.png");
    private static final Identifier ARROW_RIGHT_CROSSED_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/arrow_right_crossed.png");
    private static final Identifier ARROW_LEFT_CROSSED_TEXTURE = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/arrow_left_crossed.png");

    private static final Identifier INFO_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/recipe_book/button.png");
    private static final Identifier INFO_HOVER_ICON = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/recipe_book/button_highlighted.png");
    private static final Identifier ENCHANT_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/enchant_button.png");
    private static final Identifier ENCHANT_HOVER_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/enchant_button_highlighted.png");

    private static final Identifier ARMOR_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/pickaxe.png");
    private static final Identifier BOOK_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/smithing_template_armor_trim.png");
    private static final Identifier DUST_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/redstone_dust.png");
    private static final Identifier DYE_ICON = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/dye.png");

    // ---- Panel Settings ---- //
    int infoPanelGuiX;
    int infoPanelGuiY;
    private static final int INFO_PANEL_BG_WIDTH = 176;
    private static final int INFO_PANEL_BG_HEIGHT = 167;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    private static final int INFO_PANEL_SCROLL_WIDTH = 158;
    private static final int INFO_PANEL_SCROLL_HEIGHT = 138;
    private static final int INFO_PANEL_SCROLL_X_OFFSET = 6;
    private static final int INFO_PANEL_SCROLL_Y_OFFSET = 20;

    private final int ITEM_SLOT = 36;
    private final int BOOK_SLOT = 37;
    private final int DUST_SLOT_1 = 38;
    private final int DUST_SLOT_2 = 39;
    private final int DUST_SLOT_3 = 40;
    private final int DYE_SLOT = 41;

    int infoPanelX;
    int infoPanelY;
    private static final int INFO_PANEL_PADDING = 4;

    private InfoScrollPanel infoPanel;
    private boolean infoPanelOpen = false;

    private EnchantingMenuState clientState = EnchantingMenuState.defaults();

    private AbstractButton enchantButton;

    // ---- Constructor ---- //
    public EnchantingScreen(EnchantingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // ---- Initialization ---- //
    @Override
    protected void init() {
        super.init();

        recalcGuiPosition();

        // Compute panel positions here, when leftPos/topPos are valid
        infoPanelGuiX = leftPos + imageWidth + 8;
        infoPanelGuiY = topPos;

        infoPanelX = infoPanelGuiX + INFO_PANEL_SCROLL_X_OFFSET;
        infoPanelY = infoPanelGuiY + INFO_PANEL_SCROLL_Y_OFFSET;

        refreshInfoPanel();

        infoPanel = new InfoScrollPanel(Minecraft.getInstance(), INFO_PANEL_SCROLL_WIDTH, INFO_PANEL_SCROLL_HEIGHT, infoPanelX, infoPanelY, this.font, menu);
        reloadWidgets();
    }

    private void reloadWidgets() {
        this.clearWidgets();

        initEnchantButton();
        initInfoButton();

        if (infoPanelOpen) {
            addRenderableWidget(infoPanel);
        }
    }

    private void recalcGuiPosition() {
        int totalWidth = imageWidth + (infoPanelOpen ? INFO_PANEL_BG_WIDTH + INFO_PANEL_PADDING : 0);
        this.leftPos = (this.width - totalWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    public void setEnchantingState(EnchantingMenuState state) {
        this.clientState = state;
        ModConstants.LOGGER.info("State sent");
        refreshInfoPanel();
        updateEnchantButtonTooltip();
    }

    private void refreshInfoPanel() {
        ModConstants.LOGGER.info("refresh Info Panel");
        if (infoPanelOpen) infoPanel.refresh(clientState);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Forward vertical scroll (deltaY) to info panel if hovering
        if (infoPanel != null && infoPanel.isMouseOver(mouseX, mouseY)) {
            return infoPanel.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        // Forward to the superclass for slot scrolling
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    // ---- Enchant Button ---- //
    private void initEnchantButton() {
        int x = leftPos + 147;
        int y = topPos + 17;
        int w = 20;
        int h = 18;

        enchantButton = new AbstractButton(x, y, w, h, Component.empty()) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                ClientPacketDistributor.sendToServer(new EnchantRequestPayload(menu.containerId));
                refreshInfoPanel();
            }

            @Override
            protected void renderContents(GuiGraphics gui, int i, int i1, float v) {
                Identifier tex = isHovered() ? ENCHANT_HOVER_ICON : ENCHANT_ICON;
                gui.blit(RenderPipelines.GUI_TEXTURED, tex, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {

            }
        };

        updateEnchantButtonTooltip();

        this.addRenderableWidget(enchantButton);
    }

    private void updateEnchantButtonTooltip() {
        if (enchantButton == null) return;
        Component tooltip = clientState.getMode() == 0 ? Component.literal("Enchant Item") : Component.literal("Disenchant Item");
        enchantButton.setTooltip(Tooltip.create(tooltip));
    }

    // ---- Info Button ---- //
    private void initInfoButton() {
        int x = leftPos + 147;
        int y = topPos + 52;
        int w = 20;
        int h = 18;

        AbstractButton infoButton = new AbstractButton(x, y, w, h, Component.empty()) {
            @Override
            public void onPress(@NonNull InputWithModifiers inputWithModifiers) {
                infoPanelOpen = !infoPanelOpen;

                recalcGuiPosition();
                rebuildWidgets();
                refreshInfoPanel();
            }

            @Override
            protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float delta) {
                // Draw button texture manually
                Identifier tex = isHoveredOrFocused() ? INFO_HOVER_ICON : INFO_ICON;
                gui.blit(RenderPipelines.GUI_TEXTURED, tex, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
            }


            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {}

        };

        Component component = infoPanelOpen ? Component.literal("Hide Enchanting information") : Component.literal("Show Enchanting information");
        infoButton.setTooltip(Tooltip.create(component));

        this.addRenderableWidget(infoButton);
    }

    // ---- Rendering ---- //
    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        // Draw main GUI
        gui.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        drawSlotIcons(gui, leftPos, topPos);
        drawArrow(gui, leftPos, topPos);
    }

    private void drawSlotIcons(GuiGraphics gui, int guiLeft, int guiTop) {
        ItemStack slot0 = menu.getSlot(ITEM_SLOT).getItem();
        ItemStack slot1 = menu.getSlot(BOOK_SLOT).getItem();
        ItemStack slot2 = menu.getSlot(DUST_SLOT_1).getItem();
        ItemStack slot3 = menu.getSlot(DUST_SLOT_2).getItem();
        ItemStack slot4 = menu.getSlot(DUST_SLOT_3).getItem();
        ItemStack slot5 = menu.getSlot(DYE_SLOT).getItem();

        if (slot0.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, ARMOR_ICON, guiLeft + 123, guiTop + 18, 0, 0, 16, 16, 16, 16);
        if (slot1.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, BOOK_ICON, guiLeft + 65, guiTop + 18, 0, 0, 16, 16, 16, 16);
        if (slot2.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, DUST_ICON, guiLeft + 47, guiTop + 53, 0, 0, 16, 16, 16, 16);
        if (slot3.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, DUST_ICON, guiLeft + 65, guiTop + 53, 0, 0, 16, 16, 16, 16);
        if (slot4.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, DUST_ICON, guiLeft + 83, guiTop + 53, 0, 0, 16, 16, 16, 16);
        if (slot5.isEmpty()) gui.blit(RenderPipelines.GUI_TEXTURED, DYE_ICON, guiLeft + 123, guiTop + 53, 0, 0, 16, 16, 16, 16);
    }

    private void drawArrow(GuiGraphics gui, int guiLeft, int guiTop) {
        int x = guiLeft + 90;
        int y = guiTop + 17;

        Identifier tex = clientState.getMode() == 0 ? ARROW_RIGHT_TEXTURE : ARROW_LEFT_TEXTURE;

        gui.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);

        // compute left/top positions dynamically
        int totalWidth = imageWidth + (infoPanelOpen ? INFO_PANEL_BG_WIDTH + INFO_PANEL_PADDING : 0);
        int guiLeft = (width - totalWidth) / 2;
        int guiTop = (height - imageHeight) / 2;

        if (infoPanelOpen) {
            int panelGuiX = guiLeft + imageWidth + INFO_PANEL_PADDING;
            int panelGuiY = guiTop;

            gui.blit(RenderPipelines.GUI_TEXTURED, INFO_PANEL_TEXTURE, panelGuiX, panelGuiY, 0, 0, INFO_PANEL_BG_WIDTH, INFO_PANEL_BG_HEIGHT, 257, 257);
            gui.drawString(font, Component.literal("Enchanting Information"), panelGuiX + 8, panelGuiY + 8, -12566464, false);

            // Update info panel position so it renders in the correct spot
            infoPanel.render(gui, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index >= 36) {

            // Tooltip lines
            List<Component> itemComponent = List.of(Component.literal("Item"));
            List<Component> bookComponent = List.of(Component.literal("Book"));
            List<Component> dustComponent = List.of(Component.literal("Dust"));
            List<Component> dyeComponent = List.of(Component.literal("Dye"));

            List<Component> component;

            component = switch (hoveredSlot.index) {
                case ITEM_SLOT -> itemComponent;
                case BOOK_SLOT -> bookComponent;
                case DUST_SLOT_1, DUST_SLOT_2, DUST_SLOT_3 -> dustComponent;
                case DYE_SLOT -> dyeComponent;
                default -> throw new IllegalStateException("Unexpected value: " + hoveredSlot.index);
            };

            // Convert to ClientTooltipComponent using ClientHooks (as in GuiGraphics)
            List<ClientTooltipComponent> tooltipComponent =
                    ClientHooks.gatherTooltipComponents(ItemStack.EMPTY, component, mouseX,
                            this.width, this.height, this.font);

            // Render it
            gui.renderTooltip(this.font, tooltipComponent, mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);

        }
    }
}
