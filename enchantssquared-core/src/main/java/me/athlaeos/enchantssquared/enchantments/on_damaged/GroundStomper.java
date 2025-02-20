package me.athlaeos.enchantssquared.enchantments.on_damaged;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class GroundStomper extends CustomEnchant implements TriggerOnDamagedEnchantment, Listener {
    private final double damagePerBlockBase;
    private final double damagePerBlockLv;
    private final double radiusPerBlockBase;
    private final double radiusPerBlockLv;
    private final int minFallingDistance;
    private final double maxRadius;
    private final double maxDamage;
    private final double damageSweetSpotFalloff;
    private final double gravityMultiplier;
    private final double elytraMultiplier;
    private final int cooldownBase;
    private final int cooldownLv;
    
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
    public GroundStomper(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();

        this.damagePerBlockBase = config.getDouble("enchantment_configuration.ground_stomping.damage_block_base");
        this.damagePerBlockLv = config.getDouble("enchantment_configuration.ground_stomping.damage_block_lv");
        this.radiusPerBlockBase = config.getDouble("enchantment_configuration.ground_stomping.radius_block_base");
        this.radiusPerBlockLv = config.getDouble("enchantment_configuration.ground_stomping.radius_block_lv");
        this.minFallingDistance = config.getInt("enchantment_configuration.ground_stomping.minimum_falling_distance");
        this.maxRadius = config.getDouble("enchantment_configuration.ground_stomping.radius_max");
        this.maxDamage = config.getDouble("enchantment_configuration.ground_stomping.damage_max");
        this.damageSweetSpotFalloff = Math.max(0, Math.min(1, config.getDouble("enchantment_configuration.ground_stomping.sweet_spot_dropoff")));
        this.elytraMultiplier = config.getDouble("enchantment_configuration.ground_stomping.elytra_multiplier");
        this.gravityMultiplier = config.getDouble("enchantment_configuration.ground_stomping.gravity_multiplier");
        this.cooldownBase = config.getInt("enchantment_configuration.ground_stomping.cooldown_base");
        this.cooldownLv = config.getInt("enchantment_configuration.ground_stomping.cooldown_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.ground_stomping.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.ground_stomping.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.ground_stomping.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.ground_stomping.icon", createIcon(Material.ANVIL));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.ground_stomping.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.ground_stomping.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.ground_stomping.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.ground_stomping";
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
        return config.getInt("enchantment_configuration.ground_stomping.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.ground_stomping.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.ground_stomping.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.ground_stomping.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.ground_stomping.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.ground_stomping.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.ground_stomping.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.ground_stomping.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.ground_stomping.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.ground_stomping.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-ground-stomping";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    @Override
    public void onDamaged(EntityDamageEvent e, int level) {
        if (!(e.getEntity() instanceof LivingEntity) || (e.getCause() != EntityDamageEvent.DamageCause.FALL && e.getCause() != EntityDamageEvent.DamageCause.FLY_INTO_WALL)) return;
        LivingEntity entity = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, entity, e.getEntity().getLocation()) ||
                !CooldownManager.getInstance().isCooldownPassed(entity.getUniqueId(), "cooldown_groundstompers")) return;
        if (entity instanceof Player){
            if (!((Player) entity).isSneaking()) return;
        }
        e.setCancelled(true); // damage is cancelled regardless if effect lands
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && entity.getFallDistance() < minFallingDistance) return;

        boolean elytra = e.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL;
        double damagePerBlock = (elytra ? elytraMultiplier : 1) * (damagePerBlockBase + ((level - 1) * damagePerBlockLv));
        double radiusPerBlock = (elytra ? elytraMultiplier : 1) * (radiusPerBlockBase + ((level - 1) * radiusPerBlockLv));
        int cooldown = cooldownBase + ((level - 1) * cooldownLv);

        double speed = elytra ? entity.getVelocity().length() : entity.getFallDistance();
        double damage = Math.min(maxDamage, speed * damagePerBlock);
        double radius = Math.min(maxRadius, speed * radiusPerBlock);

        if (radius <= 0 || damage <= 0) return;
        entity.getWorld().spawnParticle(Particle.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "EXPLOSION_EMITTER" : "EXPLOSION_HUGE"), entity.getLocation(), 0);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
        for (Entity inRange : entity.getNearbyEntities(radius, radius, radius)){
            if (inRange.equals(entity) ||
                    !(inRange instanceof LivingEntity) ||
                    EntityClassificationType.isMatchingClassification(inRange.getType(), EntityClassificationType.UNALIVE)) continue;
            LivingEntity l = (LivingEntity) inRange;
            double radiusFraction = l.getLocation().distanceSquared(entity.getLocation()) / (radius * radius);
            double damageFraction = ((1 - damageSweetSpotFalloff) * (1 - radiusFraction)) + damageSweetSpotFalloff;
            l.damage(damageFraction * damage, entity);
        }

        CooldownManager.getInstance().setCooldownIgnoreIfPermission(entity, cooldown * 50, "cooldown_groundstompers");
    }

    public static final UUID GROUNDSTOMPERS_GRAVITY = UUID.fromString("02ca5359-dcc1-4c9b-9c69-c0109c7cf4f8");

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e){
        if (e.isCancelled() || !MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)) return;
        if (!e.isSneaking()) EntityUtils.removeUniqueAttribute(e.getPlayer(), "gravity_groundstompers", Attribute.GENERIC_GRAVITY);
        else {
            if (e.getPlayer().getFallDistance() <= 0) return;
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(e.getPlayer());
            int groundStompersLevel = getLevelService(false, e.getPlayer()).getLevel(equipment);
            if (groundStompersLevel <= 0) return;
            EntityUtils.addUniqueAttribute(e.getPlayer(), GROUNDSTOMPERS_GRAVITY, "gravity_groundstompers", Attribute.GENERIC_GRAVITY, gravityMultiplier, AttributeModifier.Operation.ADD_SCALAR);
        }
    }
}