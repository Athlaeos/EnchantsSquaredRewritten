package me.athlaeos.enchantssquared.hooks.valhallammo;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategory;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomEnchantmentAddModifier extends DynamicItemModifier {
    private final CustomEnchant enchantment;

    public CustomEnchantmentAddModifier(CustomEnchant enchantment) {
        super("enchantssquared_add_enchantment_" + enchantment.getType().toLowerCase(), 0, ModifierPriority.NEUTRAL);
        this.enchantment = enchantment;

        this.category = ModifierCategory.CUSTOM_ENCHANTMENTS;

        this.bigStepDecrease = 3D;
        this.bigStepIncrease = 3D;
        this.smallStepDecrease = 1D;
        this.smallStepIncrease = 1D;
        this.defaultStrength = 1;
        this.minStrength = 0;
        this.maxStrength = Integer.MAX_VALUE;
        this.description = Utils.chat("&7Adds the &e" + enchantment.getDisplayEnchantment() + " &7enchantment from &dEnchantsSquared &7to the item. " +
                "Enchantment is cancelled if item already has this enchantment. If a level of 0 is chosen, the enchantment is removed instead. ");
        this.displayName = Utils.chat("&dAdd EnchantsSquared Enchantment: " + enchantment.getDisplayEnchantment());
        this.icon = enchantment.getIcon().getType();
    }

    @Override
    public ItemStack processItem(Player player, ItemStack itemStack, int i) {
        if (strength > 0){
            if (CustomEnchantManager.getInstance().getEnchantStrength(itemStack, enchantment.getType()) == 0){
                CustomEnchantManager.getInstance().addEnchant(itemStack, enchantment.getType(), (int) strength);
            } else return null;
        } else {
            CustomEnchantManager.getInstance().removeEnchant(itemStack, enchantment.getType());
        }
        return itemStack;
    }

    @Override
    public String toString() {
        if (strength > 0){
            return Utils.chat("&7Adds &e" + enchantment.getDisplayEnchantment() + " " + Utils.toRoman((int) strength) + "&7 to the item");
        } else {
            return Utils.chat("&7Removes the " + enchantment.getDisplayEnchantment() + " enchantment from the item");
        }
    }
}
