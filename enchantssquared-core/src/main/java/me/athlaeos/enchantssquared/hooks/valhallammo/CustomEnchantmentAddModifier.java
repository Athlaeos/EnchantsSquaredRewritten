package me.athlaeos.enchantssquared.hooks.valhallammo;

import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomEnchantmentAddModifier extends DynamicItemModifier {
    private final String enchantment;
    private int level = 1;

    public CustomEnchantmentAddModifier(CustomEnchant enchantment) {
        super("enchantssquared_add_enchantment_" + enchantment.getType().toLowerCase());
        this.enchantment = enchantment.getType();
    }

    @Override
    public ItemStack getModifierIcon() {
        CustomEnchant enchantment = CustomEnchantManager.getInstance().getEnchantmentFromType(this.enchantment);
        return new ItemStack(enchantment.getIcon().getType());
    }

    @Override
    public String getDisplayName() {
        return "&fCustom Enchantment: &d" + enchantment;
    }

    @Override
    public String getDescription() {
        return "&fAdds or removes the custom enchantment " + enchantment + " to/from the item";
    }

    @Override
    public String getActiveDescription() {
        if (level > 0) return Utils.chat("&7Adds &e" + enchantment + " " + StringUtils.toRoman(level) + "&7 to the item");
        else return Utils.chat("&7Removes the " + enchantment + " enchantment from the item");
    }

    @Override
    public Collection<String> getCategories() {
        return new HashSet<>(Collections.singletonList("ENCHANTMENTS"));
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public DynamicItemModifier copy() {
        CustomEnchant enchantment = CustomEnchantManager.getInstance().getEnchantmentFromType(this.enchantment);
        CustomEnchantmentAddModifier modifier = new CustomEnchantmentAddModifier(enchantment);
        modifier.setLevel(this.level);
        modifier.setPriority(this.getPriority());
        return modifier;
    }

    @Override
    public String parseCommand(CommandSender commandSender, String[] args) {
        if (args.length != 1) return "One argument is expected: a level";
        try {
            level = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: a level. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender commandSender, int i) {
        return Arrays.asList("0", "1", "2", "3", "4", "5");
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(12, new ItemBuilder(Material.GOLD_NUGGET)
                .name("&eWhat level should it be?")
                .lore("&fSet to &e" + (level > 0 ? StringUtils.toRoman(level) : "&cremoval&f"),
                        "&6Click to add/remove levels").get()
        );
        return buttons;
    }

    @Override
    public void onButtonPress(InventoryClickEvent inventoryClickEvent, int i) {
        if (i == 12) level = Math.max(0, level + (inventoryClickEvent.isRightClick() ? -1 : 1));
    }

    @Override
    public void processItem(ModifierContext modifierContext) {
        ItemStack item = modifierContext.getItem().getItem();
        if (level > 0) CustomEnchantManager.getInstance().addEnchant(item, enchantment, level);
        else CustomEnchantManager.getInstance().removeEnchant(item, enchantment);
        modifierContext.getItem().setItem(item);
        modifierContext.getItem().setMeta(ItemUtils.getItemMeta(item));
    }
}
