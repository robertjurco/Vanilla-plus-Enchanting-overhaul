package net.jurcorobert.vanilla_plus_enchanting.common.menu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPower;
import net.jurcorobert.vanilla_plus_enchanting.common.enchanting_power.EnchantingPowerManager;
import net.jurcorobert.vanilla_plus_enchanting.common.registry.ModItems;
import net.jurcorobert.vanilla_plus_enchanting.common.utils.EnchantmentHelper;
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
    private static final float EXTRA_ENCHANT_POWER_MULTIPLIER = 1f;

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
        for (int slot = 2; slot <= 5; slot++) {
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

    public boolean hasApplicableEnchants() {return !getEnchantsToApply().isEmpty();}

    public boolean hasExtraEnchant() {return !getPossibleExtraEnchants().isEmpty();}

    public boolean hasUpgradableEnchant() {return false;}

    private int powerToApply() {
        ItemStack item = container.getItem(ITEM_SLOT);

        List<EnchantEntry> toApply = getEnchantsToApply();
        List<EnchantEntry> toRemove = getEnchantsToRemove();

        int power = EnchantingPower.get(item);

        // Remove
        for (EnchantEntry entry : toRemove)
            power += entry.power();

        // Apply
        for (EnchantEntry entry : toApply)
            power -= entry.power();

        return  power;
    }

    public boolean hasEnoughPowerToApply() {
        return powerToApply() > 0;
    }

    public boolean hasEnoughPowerForExtra() {
        return getExtraEnchant() != null;
    }

    public boolean hasEnoughPowerToUpgrade() {
        ItemStack item = container.getItem(ITEM_SLOT);

        List<EnchantEntry> toApply = getEnchantsToApply();
        List<EnchantEntry> toRemove = getEnchantsToRemove();

        int power = EnchantingPower.get(item);

        // Remove
        for (EnchantEntry entry : toRemove)
            power += entry.power();

        // Apply
        for (EnchantEntry entry : toApply)
            power -= entry.power();

        return  power > 0;
    }

    public boolean canUpgradeLevelDisenchant() {
        return !getUpgradableDisenchant().isEmpty();
    }

    public List<Holder<Enchantment>> getUpgradableDisenchant() {
        // load stuff
        ItemStack tool = container.getItem(ITEM_SLOT);
        ItemStack enchantedBook = EnchantmentHelper.getBookFromItem(tool);
        if (enchantedBook.isEmpty())
            return List.of();

        // remove curses
        if (!getCursesLocked()) {
            removeCursesFromBook(enchantedBook);
        }

        // Get current stored enchantments
        ItemEnchantments current = enchantedBook.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (current.isEmpty())
            return List.of();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);

        // Filter enchants that can be upgraded
        List<Holder<Enchantment>> upgradable = new ArrayList<>();
        for (Holder<Enchantment> holder : mutable.keySet()) {
            int level = mutable.getLevel(holder);
            int maxLevel = holder.value().definition().maxLevel();
            if (level < maxLevel + 1 && maxLevel != 1)
                upgradable.add(holder);
        }

        return upgradable;
    }


    // ----- special ----- //

    public List<EnchantEntry> getApplicableEnchantmentsFromBook() {
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


        // Prepare return
        List<EnchantEntry> applicable = new ArrayList<>();

        // Read enchantments from the book using helper
        List<Object2IntMap.Entry<Holder<Enchantment>>> enchants = new ArrayList<>(EnchantmentHelper.getItemEnchantments(book));
        Collections.shuffle(enchants, new Random());

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants) {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();

            // Check supported items
            HolderSet<Item> supported = enchantment.definition().supportedItems();
            if (!item.is(supported)) {
                continue;
            }

            // Skip if already applied or incompatible with already applied
            boolean compatible = true;
            for (EnchantEntry existing : applicable) {
                if (!Enchantment.areCompatible(existing.enchantment(), holder)) {
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
        }

        return applicable;
    }

    public List<EnchantEntry> getEnchantsToApply() {
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);

        // Basic validation
        if (item.isEmpty() || book.isEmpty() || !item.isDamageableItem() || !book.is(Items.ENCHANTED_BOOK))
            return List.of();

        // Get or create enchantment component
        ItemEnchantments current = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<Holder<Enchantment>> appliedOnItem = new HashSet<>(current.keySet());

        // Prepare return
        List<EnchantEntry> applicable = new ArrayList<>();

        // Read enchantments from the book using your helper
        for (EnchantEntry entry : getApplicableEnchantmentsFromBook()) {

            Holder<Enchantment> holder = entry.enchantment();

            // Skip if already applied or incompatible with already applied
            if (getExistingEnchantsLocked()){
                boolean compatible = true;
                for (Holder<Enchantment> existing : appliedOnItem) {
                    if (!Enchantment.areCompatible(existing, holder)) {
                        compatible = false;
                        break;
                    }
                }
                if (!compatible) continue;
            }

            // add enchant
            applicable.add(entry);
        }

        return applicable;
    }

    public List<EnchantEntry> getEnchantsToRemove() {
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);

        // Basic validation
        if (item.isEmpty() || book.isEmpty() || !item.isDamageableItem() || !book.is(Items.ENCHANTED_BOOK))
            return List.of();

        // If existing enchants are locked, nothing can be removed
        if (getExistingEnchantsLocked())
            return List.of();

        // Enchants currently on the item
        ItemEnchantments current = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<Holder<Enchantment>> appliedOnItem = new HashSet<>(current.keySet());

        // Prepare return
        List<EnchantEntry> toRemove = new ArrayList<>();

        // Enchants coming from the book
        for (EnchantEntry entry : getApplicableEnchantmentsFromBook()) {
            Holder<Enchantment> incoming = entry.enchantment();

            for (Holder<Enchantment> existing : appliedOnItem) {
                // If NOT compatible → mark existing enchant for removal
                if (!Enchantment.areCompatible(existing, incoming)) {
                    int level = current.getLevel(existing);
                    int power = EnchantingPowerManager.getEnchantPower(existing, level);
                    toRemove.add(new EnchantEntry(existing, level, power));
                }
            }
        }

        return toRemove;
    }

    public List<EnchantEntry> getPossibleExtraEnchants() {
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack stack = getMode() == 0 ? container.getItem(ITEM_SLOT) : container.getItem(BOOK_SLOT);

        // For enchanting mode we need enchants to avoid
        ItemEnchantments current = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Set<Holder<Enchantment>> appliedOnItem = new HashSet<>(current.keySet());
        List<EnchantEntry> toApply = getEnchantsToApply();

        if (stack.isEmpty())
            return List.of();

        List<EnchantEntry> result = new ArrayList<>();

        // REUSE your existing function
        for (Holder<Enchantment> holder : EnchantmentHelper.getPossibleEnchantments(stack)) {
            Enchantment enchantment = holder.value();

            // Skip curses if locked
            if (holder.is(EnchantmentTags.CURSE) && getCursesLocked())
                continue;

            // Skip if in enchanting mode and already applied or incompatible with already applied
            if (getMode() == 0){
                boolean compatible = true;

                // Applied on item
                for (Holder<Enchantment> existing : appliedOnItem) {
                    if (!Enchantment.areCompatible(existing, holder)) {
                        compatible = false;
                        break;
                    }
                }

                // In to Apply list
                for (EnchantEntry existing : toApply) {
                    if (!Enchantment.areCompatible(existing.enchantment(), holder)) {
                        compatible = false;
                        break;
                    }
                }

                if (!compatible) continue;
            }

            // Expand per-level
            int min = enchantment.getMinLevel();
            int max = enchantment.getMaxLevel();

            for (int level = min; level <= max; level++) {
                int power = EnchantingPowerManager.getEnchantPower(holder, level);

                result.add(new EnchantEntry(holder, level, Math.round(power * EXTRA_ENCHANT_POWER_MULTIPLIER)));
            }
        }
        System.out.println(result);
        return result;
    }

    public EnchantEntry getExtraEnchant() {
        int leftPower = powerToApply();
        List<EnchantEntry> possibleEnchants = getPossibleExtraEnchants();

        // Filter only the ones we can afford with remaining power
        List<EnchantEntry> affordable = new ArrayList<>();
        for (EnchantEntry entry : possibleEnchants)
            if (entry.power() <= leftPower)
                affordable.add(entry);

        // No enchant can be applied
        if (affordable.isEmpty())
            return null;

        // Pick one at random
        Random rand = new Random();
        return affordable.get(rand.nextInt(affordable.size()));
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

        book.set(DataComponents.STORED_ENCHANTMENTS, rebuilt.toImmutable());
    }

    public void applyEnchantments(ItemStack stack, List<EnchantEntry> toApply, List<EnchantEntry> toRemove) {
        if (stack.isEmpty() || toApply.isEmpty())
            return;

        int appliedPower = 0;
        int removedPower = 0;

        // Get or create enchantment component
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);

        // Remove
        for (EnchantEntry entry : toRemove) {
            Holder<Enchantment> holder = entry.enchantment();
            mutable.set(holder, 0); // level <= 0 removes enchant

            removedPower += entry.power();
        }

        // Apply
        for (EnchantEntry entry : toApply) {
            Holder<Enchantment> holder = entry.enchantment();
            int level = entry.level();

            // Apply enchant
            mutable.set(holder, level);

            // Reduce power
            appliedPower += entry.power();
        }

        // Write back to the item
        int originalPower = EnchantingPower.get(stack);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

        // Fix power
        int newPower = originalPower - appliedPower + removedPower;
        if (newPower < 0)
            throw new IllegalStateException("[Vanilla+ Enchanting] EnchantingPower underflow!\n");
        EnchantingPower.set(stack, newPower);

    }

    // ----- Main Logic ---- //

    private boolean canEnchant(){
        ItemStack item = container.getItem(ITEM_SLOT);
        ItemStack book = container.getItem(BOOK_SLOT);
        if (item.isEmpty() || book.isEmpty() || !item.isDamageableItem() || !book.is(Items.ENCHANTED_BOOK))
            return false;
        if (hasTooMuchAmethystDust() || hasTooMuchGlowstoneDust() || hasTooMuchSugar())
            return false;
        if (!hasApplicableEnchants() || !hasEnoughPowerToApply())
            return false;
        if (getExtraEnchantChance() > 0 && (!hasExtraEnchant() || !hasEnoughPowerForExtra()))
            return false;
        if (getUpgradeLevelChance() > 0 && (!hasUpgradableEnchant() || !hasEnoughPowerToUpgrade()))
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

        // Prepare item, enchanting power, list of applied enchants
        ItemStack output = item.copy();
        List<EnchantEntry> toApply = getEnchantsToApply();
        List<EnchantEntry> toRemove = getEnchantsToRemove();

        // Extra enchant
        if (rand.nextFloat() < getExtraEnchantChance()) {
            EnchantEntry extra = getExtraEnchant();
            if (extra != null) {
                // Apply the extra enchant directly
                toApply.add(extra);
            }
        }

        // apply enchants
        applyEnchantments(output, toApply, toRemove);

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

        // Upgrade level
        if (rand.nextFloat() < getUpgradeLevelChance()) {
            // Get current stored enchantments
            ItemEnchantments current = enchantedBook.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (current.isEmpty()) return;
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);

            // Pick random upgradable
            List<Holder<Enchantment>> upgradable = getUpgradableDisenchant();
            Holder<Enchantment> chosen = upgradable.get(rand.nextInt(upgradable.size()));

            int currentLevel = mutable.getLevel(chosen);
            int maxLevel = chosen.value().definition().maxLevel();

            // Upgrade level safely
            if (currentLevel < maxLevel + 1 && maxLevel != 1) {
                mutable.set(chosen, currentLevel + 1);
            }

            // Write back to the book
            enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        }

        // Extra enchant
        if (rand.nextFloat() < getExtraEnchantChance()) {
            EnchantEntry extra = getExtraEnchant();
            if (extra != null) {
                // Get current stored enchantments
                ItemEnchantments current = enchantedBook.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);

                // Add the new enchant
                mutable.set(extra.enchantment(), extra.level());

                // Write back to the book
                enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            }
        }

        // Transferred power
        int transferredPower = Math.round(EnchantingPowerManager.calculateBaseBookPower(enchantedBook) * getDisenchantEfficiency());

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

                hasNonCurseEnchantments(),

                hasApplicableEnchants(),
                hasExtraEnchant(),
                hasUpgradableEnchant(),

                hasEnoughPowerToApply(),
                hasEnoughPowerForExtra(),
                hasEnoughPowerToUpgrade(),

                canUpgradeLevelDisenchant()
        );
    }

}
