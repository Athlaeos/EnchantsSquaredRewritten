package me.athlaeos.enchantssquared.enchantments.on_attacked;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class CurseBerserk extends CustomEnchant implements TriggerOnAttackedEnchantment, TriggerOnAttackEnchantment {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public CurseBerserk(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.curse_berserk.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_berserk.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_berserk.incompatible_custom_enchantments"));

        this.damageBuffBase = config.getDouble("enchantment_configuration.curse_berserk.damage_dealt_base");
        this.damageBuffLv = config.getDouble("enchantment_configuration.curse_berserk.damage_dealt_lv");
        this.damageTakenBase = config.getDouble("enchantment_configuration.curse_berserk.damage_taken_base");
        this.damageTakenLv = config.getDouble("enchantment_configuration.curse_berserk.damage_taken_lv");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.curse_berserk.icon", new ItemStack(Material.BLAZE_POWDER));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    private final double damageBuffBase;
    private final double damageBuffLv;
    private final double damageTakenBase;
    private final double damageTakenLv;

    @Override
    public void onAttacked(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, victim, victim.getLocation())) return;

        double damageTakenMultiplier = damageTakenBase + ((level - 1) * damageTakenLv);
        e.setDamage(e.getDamage() * damageTakenMultiplier);
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        double damageDealtMultiplier = damageBuffBase + ((level - 1) * damageBuffLv);
        e.setDamage(e.getDamage() * damageDealtMultiplier);
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.curse_berserk.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.curse_berserk.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.curse_berserk.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.curse_berserk";
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
        return config.getInt("enchantment_configuration.curse_berserk.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.curse_berserk.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.curse_berserk.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.curse_berserk.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.curse_berserk.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.curse_berserk.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.curse_berserk.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.curse_berserk.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_berserk.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_berserk.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-curse-berserk";
    }
}
