package me.athlaeos.enchantssquared.hooks.valhallammo;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class GenericValhallaStatEnchantment extends CustomEnchant {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    /**
     * Constructor for a Custom Enchant. The type and id must be unique and the type will automatically be uppercased
     * by convention.
     * The id will be used on the item to store the enchantment and thus must be consistent, or it will risk
     * changing existing enchantments on item or simply invalidate the enchantment entirely.
     *
     * @param id   the identifying id of this custom enchant.
     * @param type the identifying type of this custom enchant.
     */
    public GenericValhallaStatEnchantment(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config_valhallammo.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantments." + type.toLowerCase() + ".compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantments." + type.toLowerCase() + ".incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantments." + type.toLowerCase() + ".incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantments." + type.toLowerCase() + ".icon", createIcon(Material.BOOK));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantments." + type.toLowerCase() + ".enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantments." + type.toLowerCase() + ".description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantments." + type.toLowerCase() + ".enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant." + type.toLowerCase();
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    private final Collection<String> naturallyCompatibleWith;
    @Override
    public boolean isNaturallyCompatible(Material material) {
        return MaterialClassType.isMatchingClass(material, naturallyCompatibleWith);
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return true;
    }
    
    @Override
    public int getWeight() {
        return config.getInt("enchantments." + type.toLowerCase() + ".weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantments." + type.toLowerCase() + ".max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantments." + type.toLowerCase() + ".max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantments." + type.toLowerCase() + ".is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantments." + type.toLowerCase() + ".book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantments." + type.toLowerCase() + ".trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantments." + type.toLowerCase() + ".trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantments." + type.toLowerCase() + ".trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantments." + type.toLowerCase() + ".trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantments." + type.toLowerCase() + ".trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-" + type.toLowerCase();
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
