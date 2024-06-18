package me.athlaeos.enchantssquared.enchantments.on_interact;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandOnly;
import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;

public class Shockwave extends CustomEnchant implements TriggerOnInteractEnchantment {
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
    public Shockwave(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.shockwave.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.shockwave.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.shockwave.incompatible_custom_enchantments"));

        this.forceBase = config.getDouble("enchantment_configuration.shockwave.force_base");
        this.forceLv = config.getDouble("enchantment_configuration.shockwave.force_lv");
        this.radiusBase = config.getDouble("enchantment_configuration.shockwave.radius_base");
        this.radiusLv = config.getDouble("enchantment_configuration.shockwave.radius_lv");
        this.explode = config.getBoolean("enchantment_configuration.shockwave.explode");
        this.cooldownMessage = config.getString("enchantment_configuration.shockwave.cooldown_message");
        this.cooldown = config.getInt("enchantment_configuration.shockwave.cooldown");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.shockwave.icon", createIcon(Material.TNT));
    }

    private final LevelService levelService = new LevelsFromMainHandOnly(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.shockwave.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.shockwave.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.shockwave.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.shockwave";
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
        return config.getInt("enchantment_configuration.shockwave.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.shockwave.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.shockwave.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.shockwave.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.shockwave.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.shockwave.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.shockwave.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.shockwave.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.shockwave.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.shockwave.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-torches";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final int cooldown;
    private final double forceBase;
    private final double forceLv;
    private final double radiusBase;
    private final double radiusLv;
    private final String cooldownMessage;
    private final boolean explode;

    @Override
    public void onInteract(PlayerInteractEvent e, int level) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getHand() != EquipmentSlot.HAND ||
                clickedBlock == null ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                e.getBlockFace() != BlockFace.UP ||
                !CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "shockwave_cooldown") ||
                shouldEnchantmentCancel(level, e.getPlayer(), clickedBlock.getLocation())) return;
        if (!CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "shockwave_cooldown")) {
            if (!cooldownMessage.isEmpty()){
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.chat(cooldownMessage
                        .replace("{cooldown}", String.format("%.2f", CooldownManager.getInstance().getCooldown(e.getPlayer().getUniqueId(), "shockwave_cooldown") / 1000D)))));
            }
            return;
        }

        double radius = radiusBase + ((level - 1) * radiusLv);
        double force = forceBase + ((level - 1) * forceLv);
        Location origin = clickedBlock.getLocation().add(1, 0.5, 1);
        Collection<Entity> entitiesInRadius = e.getPlayer().getWorld().getNearbyEntities(origin,
                radius, radius, radius);
        entitiesInRadius.remove(e.getPlayer());

        for (Entity entity : entitiesInRadius){
            if (entity instanceof LivingEntity && !EntityClassificationType.isMatchingClassification(entity.getType(), EntityClassificationType.UNALIVE)){
                ((LivingEntity) entity).damage(0.01, e.getPlayer());
                AttributeInstance attribute = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                double forceWithResistance = force;
                if (attribute != null) forceWithResistance *= Math.max(0, 1 - attribute.getValue());

                entity.setVelocity(new Vector(
                        (entity.getLocation().getX() - origin.getX()) * forceWithResistance,
                        forceWithResistance,
                        (entity.getLocation().getZ() - origin.getZ()) * forceWithResistance));
            }
        }
        if (explode) {
            e.getPlayer().getWorld().spawnParticle(Particle.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "EXPLOSION" : "EXPLOSION_HUGE"), origin, 0);
            e.getPlayer().getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.5F);
        }
        CooldownManager.getInstance().setCooldownIgnoreIfPermission(e.getPlayer(), cooldown * 50, "shockwave_cooldown");
    }
}
