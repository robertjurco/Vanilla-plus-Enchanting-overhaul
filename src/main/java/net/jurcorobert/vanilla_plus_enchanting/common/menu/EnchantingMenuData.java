package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.jurcorobert.vanilla_plus_enchanting.common.utils.EnchantmentHelper;
import net.jurcorobert.vanilla_plus_enchanting.common.villager.EnchantedItemTradePool;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModConstants;
import net.jurcorobert.vanilla_plus_enchanting.constants.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.fml.common.Mod;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class EnchantingMenuData {

    // ---- Slots ---- //
    public static final Container container = new SimpleContainer(6) {

        @Override
        public boolean canPlaceItem(int slot, @NonNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.isDamageableItem(); // tool
                case 1 -> stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK); // book
                case 2, 3, 4 -> stack.is(ModTags.Items.CUSTOM_ENCHANTING_INGREDIENTS); // modifiers
                case 5 -> stack.getItem() instanceof DyeItem; // new slot for dye
                default -> false;
            };
        }
    };

    private static final int ITEM_SLOT = 0;
    private static final int BOOK_SLOT = 1;
    private static final int DUST_SLOT_1 = 2;
    private static final int DUST_SLOT_3 = 3;
    private static final int DUST_SLOT_2 = 4;
    private static final int DYE_SLOT = 5;

    private static final int REQUIRED_BOOKSHELVES = 16;

    private final BlockPos pos;
    private final ServerLevel level;

    public EnchantingMenuData(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    public boolean isItemValidForSlotClient(int slot, ItemStack stack) {
        return container.canPlaceItem(slot, stack);
    }

    // ---- Dust / Modifier Utilities ---- //

    private void consumeOneDust() {
        for (int slot = 2; slot <= 4; slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) container.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private int countDust(Item item) {
        int count = 0;
        for (int slot = 2; slot <= 4; slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) count++;
        }
        return count;
    }

    // ----- Getters ---- //

    public int getMode() {
        return countDust(ModItems.DIAMOND_DUST.get()) > 0 ? 1 : 0;
    }

    public float getToolBreakChance() {
        return countDust(Items.GLOWSTONE_DUST) == 1 ? 0f : 0.05f;
    }

    public float getFailChance() {
        int redstone = countDust(Items.REDSTONE);
        return redstone > 0 ? 0.15f - 0.05f * redstone : 0.15f;
    }

    private float getReducePowerMultiplier() {
        return Math.min(1f, 1f - 0.1f * countDust(ModItems.ENCHANTING_POWDER.get()));
    }

    public float getExtraEnchantChance() {
        return 0.25f * countDust(Items.GUNPOWDER);
    }

    public float getUpgradeLevelChance() {
        return 0.2f * countDust(ModItems.NETHERITE_POWDER.get());
    }

    private float getDisenchantEfficiency() {
        return Math.min(1f, 1f - 0.15f * countDust(ModItems.ECHO_POWDER.get()));
    }

    public boolean getCursesLocked() {
        return countDust(Items.SUGAR) > 0;
    }

    public boolean getExistingEnchantsLocked() {
        return countDust(ModItems.AMETHYST_POWDER.get()) > 0;
    }

    // ----- "has" - functions ---- //

    public boolean hasTooMuchSugar() {return countDust(Items.SUGAR) > 1;}

    public boolean hasTooMuchGlowstoneDust() {
        return countDust(Items.GLOWSTONE_DUST) > 1;
    }

    public boolean hasTooMuchDiamondDust() {return countDust(ModItems.DIAMOND_DUST.get()) > 1;}

    public boolean hasTooMuchAmethystDust() {return countDust(ModItems.AMETHYST_POWDER.get()) > 1;}

    public boolean hasApplicableEnchantments() {return !getApplicableEnchantments().isEmpty();}

    public boolean hasNonCurseEnchantments() {
        ItemStack item = container.getItem(ITEM_SLOT);
        if (item.isEmpty() || !item.isDamageableItem())
            return false;

        ItemEnchantments enchantments = item.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty())
            return false;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE))
                return true;
        }

        return false;
    }

    // ----- special ----- //

    public List<EnchantEntry> getApplicableEnchantments() {
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);

        // Basic validation
        if (item.isEmpty() || book.isEmpty() || !item.isDamageableItem() || !book.is(Items.ENCHANTED_BOOK)) {
            return List.of();
        }

        // Enchanting power coefficient
        int basePower = EnchantingPowerManager.calculateBaseBookPower(book);
        int realPower = EnchantingPower.get(book);
        if (basePower <= 0 || realPower <= 0)
            return List.of();
        float coef = (float) realPower / basePower;


        // Get or create enchantment component
        ItemEnchantments current = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Prepare return
        Set<Holder<Enchantment>> appliedAndApplicable = new HashSet<>(current.keySet());
        List<EnchantEntry> applicable = new ArrayList<>();

        // Read enchantments from the book using your helper
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getItemEnchantments(book)) {

            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();

            // Check supported items
            HolderSet<Item> supported = enchantment.definition().supportedItems();
            if (!item.is(supported)) {
                continue;
            }

            // Skip if already applied or incompatible with already applied
            boolean compatible = true;
            for (Holder<Enchantment> existing : appliedAndApplicable) {
                if (!Enchantment.areCompatible(existing, holder)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) continue;


            // Skip curses if curses are locked
            if (holder.is(EnchantmentTags.CURSE) && getCursesLocked())
                continue;

            // add enchant
            int level = entry.getIntValue();
            int power = EnchantingPowerManager.getEnchantPower(holder, level);
            applicable.add(new EnchantEntry(holder, level, Math.round(power * coef * getReducePowerMultiplier())));
            appliedAndApplicable.add(holder);
        }

        System.out.println(applicable);
        return applicable;
    }

    private void  removeCursesFromBook(ItemStack book) {
        ItemEnchantments enchants = book.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return;

        ItemEnchantments.Mutable rebuilt = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
            Holder<Enchantment> ench = entry.getKey();
            int level = entry.getIntValue();

            if (ench.is(EnchantmentTags.CURSE))
                continue;

            rebuilt.set(ench, level);
        }
    }

    public void applyEnchantments(ItemStack stack, List<EnchantEntry> enchantments) {
        if (stack.isEmpty() || enchantments.isEmpty())
            return;

        int appliedPower = 0;

        // Get or create enchantment component
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);

        // Track already applied holders for compatibility

        for (EnchantEntry entry : enchantments) {
            Holder<Enchantment> holder = entry.enchantment();
            int level = entry.level();

            // Apply enchant
            mutable.set(holder, level);

            // Reduce power
            appliedPower += entry.power;
        }

        // Write back to the item
        int originalPower = EnchantingPower.get(stack);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        EnchantingPower.set(stack, originalPower - appliedPower);
    }

    // ----- Main Logic ---- //

    private boolean canEnchant(){
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);
        if (item.isEmpty() || book.isEmpty() || !item.isDamageableItem() || !book.is(Items.ENCHANTED_BOOK))
            return false;
        if (hasTooMuchAmethystDust() || hasTooMuchGlowstoneDust() || hasTooMuchSugar())
            return false;
        if (!hasApplicableEnchantments())
            return false;
        // power check
        return true;
    }

    private boolean canDisenchant(){
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);
        if (item.isEmpty() || book.isEmpty() || !book.is(Items.BOOK) || !item.isEnchanted())
            return false;
        if (hasTooMuchDiamondDust() || hasTooMuchGlowstoneDust() || hasTooMuchSugar())
            return false;
        if (getCursesLocked() && !hasNonCurseEnchantments())
            return false;
        return true;
    }

    private void enchantItem(){
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);
        if (item.isEmpty() || book.isEmpty()) return;

        // random
        Random rand = new Random();

        // Fail chance
        if (rand.nextFloat() < getFailChance()) {
            consumeOneDust();
            container.setItem(BOOK_SLOT, ItemStack.EMPTY);
            return;
        }

        // tool break chance
        if (rand.nextFloat() < getToolBreakChance()) {
            container.setItem(ITEM_SLOT, ItemStack.EMPTY);
            container.setItem(BOOK_SLOT, ItemStack.EMPTY);
            consumeOneDust();
            return;
        }

        // prepare item, enchanting power, list of applied enchants
        int remainingPower = EnchantingPower.get(item);
        ItemStack output = item.copy();
        List<EnchantEntry> toApply = getApplicableEnchantments();


        // apply enchants
        applyEnchantments(output, toApply);

        // set output
        container.setItem(ITEM_SLOT, output);
        container.setItem(BOOK_SLOT, ItemStack.EMPTY);
        consumeOneDust();
    }

    private void disenchantItem(){
        ItemStack tool = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);
        ItemStack enchantedBook = EnchantmentHelper.getBookFromItem(tool);
        if (enchantedBook.isEmpty()) return;

        // random
        Random rand = new Random();

        // tool break chance
        if (rand.nextFloat() < getToolBreakChance()) {
            // break item
            container.setItem(ITEM_SLOT, ItemStack.EMPTY);

            // take 1 book
            ItemStack stack = container.getItem(BOOK_SLOT);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) container.setItem(BOOK_SLOT, ItemStack.EMPTY);
            }

            // and 1 dust of each
            consumeOneDust();
            return;
        }

        // Fail chance
        if (rand.nextFloat() < getFailChance()) {
            consumeOneDust();
            ItemStack stack = container.getItem(BOOK_SLOT);
            if (!stack.isEmpty()) {
                stack.shrink(BOOK_SLOT);
                if (stack.isEmpty()) container.setItem(BOOK_SLOT, ItemStack.EMPTY);
            }
            return;
        }

        // REMOVE CURSES FIRST
        if (!getCursesLocked()) {
            removeCursesFromBook(enchantedBook);
        }

        // Transferred power
        int transferredPower = Math.round(EnchantingPowerManager.calculateBaseBookPower(enchantedBook) * getDisenchantEfficiency());

        // Netherite upgrade
        // transferredPower += tryUpgradeBookEnchant(enchantedBook);

        // Gunpowder extra enchant
        // transferredPower += applyGunpowderExtraEnchantToBook(enchantedBook);

        // set enchants to book
        EnchantingPower.set(enchantedBook, transferredPower);

        container.setItem(0, ItemStack.EMPTY);
        if (!book.isEmpty()) book.shrink(BOOK_SLOT);
        if (book.isEmpty()) container.setItem(BOOK_SLOT, ItemStack.EMPTY);
        consumeOneDust();

        // set output
        container.setItem(ITEM_SLOT, enchantedBook);
    }

    public void craftItemServer()  {
        int mode = getMode();
        if (mode == 1 && canDisenchant())
            disenchantItem();
        if (mode == 0 && canEnchant())
            enchantItem();
    }

    // ----- Record ---- //
    public record EnchantEntry(
            Holder<Enchantment> enchantment,
            int level,
            int power
    ) {}

    // ----- State ---- //
    public EnchantingMenuState getState() {
        return new EnchantingMenuState(
                getMode(),
                getToolBreakChance(),
                getFailChance(),
                getReducePowerMultiplier(),
                getExtraEnchantChance(),
                getUpgradeLevelChance(),
                getDisenchantEfficiency(),
                getCursesLocked(),
                getExistingEnchantsLocked(),

                hasTooMuchSugar(),
                hasTooMuchGlowstoneDust(),
                hasTooMuchDiamondDust(),
                hasTooMuchAmethystDust(),
                hasApplicableEnchantments(),
                hasNonCurseEnchantments(),

                canEnchant(),
                canDisenchant()
        );
    }

}
