package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class AOEArrows extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double aoe_damage_base;
    private final double aoe_damage_lv;
    private final double radius_base;
    private final double radius_lv;
    private final boolean explosion;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public AOEArrows(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.aoe_damage_base = config.getDouble("enchantment_configuration.aoe_arrows.aoe_damage_base");
        this.aoe_damage_lv = config.getDouble("enchantment_configuration.aoe_arrows.aoe_damage_lv");
        this.radius_base = config.getDouble("enchantment_configuration.aoe_arrows.radius_base");
        this.radius_lv = config.getDouble("enchantment_configuration.aoe_arrows.radius_lv");
        this.explosion = config.getBoolean("enchantment_configuration.aoe_arrows.explosion");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.aoe_arrows.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.aoe_arrows.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.aoe_arrows.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.aoe_arrows.icon", createIcon(Material.SPECTRAL_ARROW));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    private final Collection<UUID> ignoreArrows = new HashSet<>();

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (ignoreArrows.contains(e.getDamager().getUniqueId()) || shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        double finalRadius = this.radius_base + ((level - 1) * radius_lv);
        double finalDamage = this.aoe_damage_base + ((level - 1) * aoe_damage_lv);
        double damage = e.getDamage();
        if (explosion){
            realAttacker.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, victim.getLocation(), 0);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
        }
        Collection<Entity> surroundingEntities = victim.getWorld().getNearbyEntities(victim.getLocation(), finalRadius, finalRadius, finalRadius);
        surroundingEntities.remove(victim);
        surroundingEntities.remove(e.getDamager());
        surroundingEntities.remove(realAttacker);
        ignoreArrows.add(e.getDamager().getUniqueId());
        for (Entity entity : surroundingEntities){
            if (entity instanceof LivingEntity && !EntityClassificationType.isMatchingClassification(entity.getType(), EntityClassificationType.UNALIVE)){
                ((LivingEntity) entity).damage(damage * finalDamage, e.getDamager());
            }
        }
        ignoreArrows.remove(e.getDamager().getUniqueId());
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.aoe_arrows.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.aoe_arrows.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.aoe_arrows.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.aoe_arrows";
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
        return material == Material.BOW || material == Material.CROSSBOW;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.aoe_arrows.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.aoe_arrows.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.aoe_arrows.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.aoe_arrows.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.aoe_arrows.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.aoe_arrows.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.aoe_arrows.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.aoe_arrows.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.aoe_arrows.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.aoe_arrows.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-aoe-arrows";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
