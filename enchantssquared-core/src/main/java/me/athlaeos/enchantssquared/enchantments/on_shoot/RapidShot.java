package me.athlaeos.enchantssquared.enchantments.on_shoot;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class RapidShot extends CustomEnchant implements TriggerOnProjectileEventEnchantment, TriggerOnAttackEnchantment {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public RapidShot(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.chance_base = config.getDouble("enchantment_configuration.rapid_shot.chance_base");
        this.chance_lv = config.getDouble("enchantment_configuration.rapid_shot.chance_lv");
        this.damage_multiplier_base = config.getDouble("enchantment_configuration.rapid_shot.damage_multiplier_base");
        this.damage_multiplier_lv = config.getDouble("enchantment_configuration.rapid_shot.damage_multiplier_lv");
        this.count_base = config.getInt("enchantment_configuration.rapid_shot.count_base");
        this.count_lv = config.getInt("enchantment_configuration.rapid_shot.count_lv");
        this.shot_delay = config.getInt("enchantment_configuration.rapid_shot.shot_delay");

        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.rapid_shot.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.rapid_shot.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.rapid_shot.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.rapid_shot.icon", createIcon(Material.TIPPED_ARROW));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    private final double chance_base;
    private final double chance_lv;
    private final double damage_multiplier_base;
    private final double damage_multiplier_lv;
    private final int count_base;
    private final int count_lv;
    private final int shot_delay;

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        if (e.getDamager() instanceof Arrow){
            if (e.getDamager().hasMetadata("ignores_immunity_frames")){
                EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> ((LivingEntity) e.getEntity()).setNoDamageTicks(0), 1L);
            }
        }
    }

    private static final Collection<UUID> activeRapidShooters = new HashSet<>();

    public static Collection<UUID> getActiveRapidShooters() {
        return activeRapidShooters;
    }

    @Override
    public void onShoot(ProjectileLaunchEvent e, int level, LivingEntity shooter) {

    }

    private void removeImmunityFrames(AbstractArrow arrow){
        arrow.setMetadata("ignores_immunity_frames", new FixedMetadataValue(EnchantsSquared.getPlugin(), true));
    }

    @Override
    public void onHit(ProjectileHitEvent e, int level, LivingEntity shooter) {

    }

    @Override
    public void onBowShot(EntityShootBowEvent e, int level, LivingEntity shooter) {
        // do not if projectile isnt an arrow
        if (shouldEnchantmentCancel(level, shooter, shooter.getLocation()) ||
                !(e.getProjectile() instanceof Arrow) ||
                activeRapidShooters.contains(shooter.getUniqueId())) return; // don't do anything to rapid arrows
        Arrow arrow = (Arrow) e.getProjectile();
        if (arrow.hasCustomEffects() || e.getForce() < 0.9) return;

        double chance = chance_base + ((level - 1) * chance_lv);
        if (Utils.getRandom().nextDouble() <= chance){
            double damageMultiplier = damage_multiplier_base + ((level - 1) * damage_multiplier_lv);
            int arrowCount = (count_base + ((level - 1) * count_lv)) - 1;
            // enchantment can only work with at least 2 arrows and we subtract 1 for the original arrow
            if (arrowCount <= 0) return;
            activeRapidShooters.add(shooter.getUniqueId());
            double reducedDamage = arrow.getDamage() * damageMultiplier;
            arrow.setDamage(reducedDamage);
            removeImmunityFrames(arrow);

            double speed = arrow.getVelocity().length();

            new BukkitRunnable(){
                int arrows = arrowCount;

                @Override
                public void run() {
                    Vector direction = shooter.getEyeLocation().getDirection().normalize().multiply(speed);
                    Arrow newArrow = shooter.launchProjectile(arrow.getClass(), direction);
                    newArrow.setDamage(reducedDamage);
                    if (arrows > 1) removeImmunityFrames(newArrow); // last arrow should not be exempt from immunity frame removal
                    EntityShootBowEvent event = new EntityShootBowEvent(shooter, e.getBow(), e.getConsumable(), newArrow, e.getHand(), e.getForce(), false);
                    EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        newArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1F, 1F);
                    } else {
                        newArrow.remove();
                    }

                    arrows--;
                    if (arrows <= 0) {
                        activeRapidShooters.remove(shooter.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(EnchantsSquared.getPlugin(), shot_delay, shot_delay);
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.rapid_shot.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.rapid_shot.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.rapid_shot.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.rapid_shot";
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
        return config.getInt("enchantment_configuration.rapid_shot.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.rapid_shot.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.rapid_shot.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.rapid_shot.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.rapid_shot.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.rapid_shot.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.rapid_shot.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.rapid_shot.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.rapid_shot.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.rapid_shot.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-rapid-shot";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
