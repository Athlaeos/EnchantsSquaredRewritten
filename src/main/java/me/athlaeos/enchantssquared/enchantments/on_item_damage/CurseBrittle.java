package me.athlaeos.enchantssquared.enchantments.on_item_damage;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.Levels1IfPresent;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CurseBrittle extends CustomEnchant implements TriggerOnItemDamageEnchantment {
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
    public CurseBrittle(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.curse_brittle.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_brittle.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_brittle.incompatible_custom_enchantments"));

        this.damageMultiplierBase = config.getDouble("enchantment_configuration.curse_brittle.damage_multiplier_base");
        this.damageMultiplierLv = config.getDouble("enchantment_configuration.curse_brittle.damage_multiplier_lv");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.curse_brittle.icon", createIcon(Material.GOLDEN_SWORD));
    }

    private final LevelService levelService = new Levels1IfPresent(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.curse_brittle.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.curse_brittle.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.curse_brittle.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.curse_brittle";
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
        return material.getMaxDurability() > 0;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.curse_brittle.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.curse_brittle.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.curse_brittle.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.curse_brittle.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.curse_brittle.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.curse_brittle.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.curse_brittle.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.curse_brittle.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_brittle.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_brittle.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-curse-brittle";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double damageMultiplierBase;
    private final double damageMultiplierLv;

    @Override
    public void onItemDamage(PlayerItemDamageEvent e, int level) {
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getPlayer().getLocation())) return;

        EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(e.getPlayer());
        Map<ItemStack, Map<CustomEnchant, Integer>> equipment = cachedEquipment.getIterableWithEnchantments(true);
        if (equipment.containsKey(e.getItem())){
            int brittleLevel = equipment.getOrDefault(e.getItem(), new HashMap<>()).getOrDefault(this, 0);
            if (brittleLevel > 0){
                e.setDamage(Utils.excessChance(e.getDamage() * (damageMultiplierBase + (damageMultiplierLv * (brittleLevel - 1)))));
            }
        }
    }
}
