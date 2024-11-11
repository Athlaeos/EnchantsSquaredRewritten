package me.athlaeos.enchantssquared.enchantments.on_death;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.listeners.EntityDeathListener;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Exploding extends CustomEnchant implements TriggerOnDeathEnchantment {
    private final double radiusBase;
    private final double radiusLv;
    private final double damageBase;
    private final double damageLv;
    private final int durationBase;
    private final int durationLv;
    private final boolean pulse;
    
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
    public Exploding(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.radiusBase = config.getDouble("enchantment_configuration.exploding.radius_base");
        this.radiusLv = config.getDouble("enchantment_configuration.exploding.radius_lv");
        this.damageBase = config.getDouble("enchantment_configuration.exploding.damage_base");
        this.damageLv = config.getDouble("enchantment_configuration.exploding.damage_lv");
        this.durationBase = config.getInt("enchantment_configuration.exploding.duration_base");
        this.durationLv = config.getInt("enchantment_configuration.exploding.duration_lv");
        this.pulse = config.getBoolean("enchantment_configuration.exploding.pulse");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.exploding.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.exploding.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.exploding.incompatible_custom_enchantments"));
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.exploding.icon", createIcon(Material.MAGMA_CREAM));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.exploding.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.exploding.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.exploding.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.exploding";
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
        return config.getInt("enchantment_configuration.exploding.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.exploding.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.exploding.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.exploding.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.exploding.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.exploding.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.exploding.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.exploding.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.exploding.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.exploding.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-exploding";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    @Override
    public void onEntityDeath(EntityDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onEntityKilled(EntityDeathEvent e, int level) {
        Player killer = e.getEntity().getKiller() == null ? EntityDeathListener.getResponsible(e.getEntity().getUniqueId()) : e.getEntity().getKiller();
        if (killer == null || shouldEnchantmentCancel(level, killer, e.getEntity().getLocation())) return;
        int duration = durationBase + ((level - 1) * durationLv);
        double damage = damageBase + ((level - 1) * damageLv);
        double radius = radiusBase + ((level - 1) * radiusLv);

        if (pulse){
            Collection<Location> pulse = MathUtils.getRandomPointsOnCircleCircumference(e.getEntity().getLocation(), radius, (int) Math.round(radius * radius) * 8, false);
            for (Location l : pulse){
                e.getEntity().getWorld().spawnParticle(Particle.FLAME, e.getEntity().getLocation(), 0,
                        (l.getX() - e.getEntity().getLocation().getX()) * 0.1,
                        0,
                        (l.getZ() - e.getEntity().getLocation().getZ()) * 0.1
                );
            }
        }
        Map<UUID, Double> healthTracker = new HashMap<>();
        for (Entity entity : e.getEntity().getNearbyEntities(radius, radius, radius)){
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity l = (LivingEntity) entity;
            if (EntityClassificationType.isMatchingClassification(l.getType(), EntityClassificationType.UNALIVE) ||
                    l.equals(killer) || l.equals(e.getEntity()) || l.isDead() || !l.isValid()) continue;
            healthTracker.put(l.getUniqueId(), l.getHealth());
            l.damage(damage, killer);
            EntityDeathListener.setResponsible(entity.getUniqueId(), killer.getUniqueId(), (duration * 50L) + 500L); // setting responsibility for duration plus half a second of wiggleroom
        }

        EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
            for (UUID uuid : healthTracker.keySet()){
                LivingEntity l = (LivingEntity) EnchantsSquared.getPlugin().getServer().getEntity(uuid);
                // checking if damage event caused by damage tick actually went through, otherwise entity wasnt damaged
                if (l == null || l.isDead() || !l.isValid() || l.getHealth() >= healthTracker.get(uuid)) continue;
                l.setFireTicks(Math.max(l.getFireTicks(), duration));
            }
        }, 2L);
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerKilled(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent e, int level) {
        // do nothing
    }
}
