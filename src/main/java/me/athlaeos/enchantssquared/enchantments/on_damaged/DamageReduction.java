package me.athlaeos.enchantssquared.enchantments.on_damaged;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.enchantments.on_attacked.TriggerOnAttackedEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class DamageReduction extends CustomEnchant implements TriggerOnAttackedEnchantment {
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
    public DamageReduction(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.damage_reduction.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.damage_reduction.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.damage_reduction.incompatible_custom_enchantments"));

        this.damageReductionBase = config.getDouble("enchantment_configuration.damage_reduction.reduction_base");
        this.damageReductionLv = config.getDouble("enchantment_configuration.damage_reduction.reduction_lv");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.damage_reduction.icon", new ItemStack(Material.DRAGON_HEAD));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.damage_reduction.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.damage_reduction.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.damage_reduction.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.damage_reduction";
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
        return config.getInt("enchantment_configuration.damage_reduction.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.damage_reduction.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.damage_reduction.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.damage_reduction.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.damage_reduction.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.damage_reduction.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.damage_reduction.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.damage_reduction.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.damage_reduction.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.damage_reduction.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-damage-reduction";
    }

    private final double damageReductionBase;
    private final double damageReductionLv;

    @Override
    public void onAttacked(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        if (!(e instanceof LivingEntity) || e.getCause() == EntityDamageEvent.DamageCause.VOID) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e.getEntity(), e.getEntity().getLocation())) return;

        double damageMultiplier = Math.max(0, 1 - (damageReductionBase + ((level - 1) * damageReductionLv)));

        e.setDamage(e.getDamage() * damageMultiplier);
    }
}