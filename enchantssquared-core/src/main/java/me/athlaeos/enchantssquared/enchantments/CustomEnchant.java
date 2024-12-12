package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.ExecutionPriority;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CustomEnchant {
    private static Map<String, String> equipmentTranslations = new HashMap<>();

    static {
        YamlConfiguration config = ConfigManager.getInstance().getConfig("translations.yml").get();
        ConfigurationSection section = config.getConfigurationSection("equipment_translations");
        if (section != null){
            Map<String, String> translations = new HashMap<>();
            for (String equipment : section.getKeys(false)){
                translations.put(equipment, config.getString("equipment_translations." + equipment));
            }
            equipmentTranslations = translations;
        }
    }

    protected String type;
    protected int id;
    protected ExecutionPriority priority = ExecutionPriority.NORMAL;

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    /**
     * Constructor for a Custom Enchant. The type and id must be unique and the type will automatically be uppercased
     * by convention.
     * The id will be used on the item to store the enchantment and thus must be consistent, or it will risk
     * changing existing enchantments on item or simply invalidate the enchantment entirely.
     * @param id the identifying id of this custom enchant.
     * @param type the identifying type of this custom enchant.
     */
    public CustomEnchant(int id, String type){
        this.id = id;
        this.type = type.toUpperCase();
    }

    public ExecutionPriority getPriority() {
        return priority;
    }

    /**
     * Returns the level gathering algorithm this particular enchantment should use.
     * For example, some enchantments may only be interested in the level on the main hand item.
     * Others may look at the players' entire gear and pick the strongest enchantment from there,
     * or combine the levels on the player's gear.
     * @param offHand if this LevelService is relevant to hand usage, main hand/off hand can be used to determine levels
     *                base on in which equipment slot the enchanted item was used
     * @return the LevelService used for gathering an enchantment's strength during execution
     */
    public abstract LevelService getLevelService(boolean offHand, LivingEntity entity);

    /**
     * @return the string representing the enchantment for display on the item's lore
     */
    public abstract String getDisplayEnchantment();

    /**
     * @return the description describing the enchantment
     */
    public abstract String getDescription();

    public void onPluginEnable(){

    }

    public abstract boolean isEnabled();

    /**
     * The permission required in order to have this custom enchant appear in the crafting table.
     * Not implemented by default, just for those who want it.
     * @return the permission required to be able to find this enchantment in an enchanting table. Can be null.
     */
    public abstract String getRequiredPermission();

    /**
     * @return if this enchantment is incompatible with another enchantment. The string can represent a custom enchantment OR a vanilla enchantment key
     */
    public abstract boolean conflictsWithEnchantment(String enchantment);
    /**
     * @return true if the custom enchant can naturally appear on an item of this material during enchanting.
     */
    public abstract boolean isNaturallyCompatible(Material material);
    /**
     * @return true if the custom enchant is functional on an item of this material. If it's not functional it shouldn't
     * do anything, and it shouldn't appear on items during enchanting aside from enchanted books.
     */
    public abstract boolean isFunctionallyCompatible(Material material);

    /**
     * @return the weighted rarity of this enchantment.
     */
    public abstract int getWeight();

    /**
     * @return the max level of the enchantment denoting how far it can be leveled using an anvil.
     * This value should not be lower than getMaxLevelTable().
     */
    public abstract int getMaxLevel();

    /**
     * @return the max level that can be reached using the enchantment table.
     */
    public abstract int getMaxTableLevel();

    /**
     * @return if the enchantment is a treasure enchantment or not. If it is, it cannot appear on the enchantment
     * table but it CAN appear in dungeon chests and villager trades and stuff
     */
    public abstract boolean isTreasure();

    /**
     * @return if the enchantment can only be obtained on books
     */
    public abstract boolean isBookOnly();

    /**
     * @return if the enchantment is obtainable through villager trades
     */
    public abstract boolean isTradingEnabled();

    /**
     * @return minimum price for a level 1 enchanted book, the price is randomized between the minimum and maximum base.
     */
    public abstract int getTradingMinBasePrice();

    /**
     * @return maximum price for a level 1 enchanted book, the price is randomized between the minimum and maximum base
     */
    public abstract int getTradingMaxBasePrice();

    /**
     * @return price added to the minimum base per level above 1
     */
    public abstract int getTradingMinLeveledPrice();

    /**
     * @return price added to the maximum base per level above 1
     */
    public abstract int getTradingMaxLeveledPrice();

    public boolean isCurse(){
        return false;
    }

    /**
     * @return the icon the enchantment should be represented with in the enchantment overview menu
     */
    public abstract ItemStack getIcon();

    public abstract String getWorldGuardFlagName();

    public boolean shouldEnchantmentCancel(int level, LivingEntity actor, Location worldGuardLocation){
        return level <= 0 || !EnchantsSquared.isWorldGuardAllowed(actor, worldGuardLocation, getWorldGuardFlagName());
    }

    public abstract Collection<String> getCompatibleItems();

    public ItemStack createIcon(Material defaultMaterial){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("translations.yml").get();

        ItemStack icon = new ItemStack(defaultMaterial);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return null;
        meta.setDisplayName(ChatUtils.chat(getDisplayEnchantment()));
        int maxLength = config.getInt("enchantment_menu.max_length");
        List<String> format = config.getStringList("enchantment_menu.lore");
        List<String> lore = new ArrayList<>();
        format.forEach(l -> {
            if (l.contains("%description%")){
                lore.addAll(ChatUtils.seperateStringIntoLines(ChatUtils.chat(getDescription()), maxLength));
            } else if (l.contains("%compatible_with%")){
                lore.addAll(ChatUtils.seperateStringIntoLines(ChatUtils.chat(
                        l.replace("%compatible_with%", "") +
                                getCompatibleItems().stream()
                                .filter(equipmentTranslations::containsKey)
                                .map(equipmentTranslations::get)
                                .collect(Collectors.joining(", "))),
                        35));
            } else {
                lore.add(ChatUtils.chat(l
                        .replace("%weight%", "" + getWeight())
                        .replace("%lv_roman%", ChatUtils.toRoman(getMaxLevel()))
                        .replace("%lv_number%", "" + getMaxLevel())));
            }
        });
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "HIDE_ADDITIONAL_TOOLTIP" : "HIDE_POTION_EFFECTS"), ItemFlag.HIDE_DYE);
        icon.setItemMeta(meta);
        return icon;
    }

    public boolean hasPermission(Permissible p){
        boolean required = CustomEnchantManager.getInstance().isRequirePermissions();
        if (!required) return true;
        return p.hasPermission("es.enchant.*") || p.hasPermission(getRequiredPermission());
    }
}
