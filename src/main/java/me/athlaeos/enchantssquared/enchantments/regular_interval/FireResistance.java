package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.Levels1IfPresent;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;

public class FireResistance extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
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
    public FireResistance(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.fire_resistance.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.fire_resistance.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.fire_resistance.incompatible_custom_enchantments"));

        this.slow = config.getDouble("enchantment_configuration.fire_resistance.slow");
        this.duration = config.getInt("enchantment_configuration.fire_resistance.duration");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.fire_resistance.icon", createIcon(Material.MAGMA_CREAM));
    }

    private final LevelService levelService = new Levels1IfPresent(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.fire_resistance.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.fire_resistance.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.fire_resistance.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.fire_resistance";
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
        return config.getInt("enchantment_configuration.fire_resistance.weight");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMaxTableLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.fire_resistance.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.fire_resistance.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.fire_resistance.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.fire_resistance.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.fire_resistance.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.fire_resistance.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.fire_resistance.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-jump";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double slow;
    private final int duration;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_fire_resistance_slow", Attribute.GENERIC_MOVEMENT_SPEED);
            return;
        }
        LivingEntity entity = (LivingEntity) e;

        EntityUtils.applyPotionEffectIfStronger(entity,
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, true, false, false)
        );
        if (e.getFireTicks() > 0){
            EntityUtils.addUniqueAttribute(entity, "es_fire_resistance_slow", Attribute.GENERIC_MOVEMENT_SPEED, -slow,
                    AttributeModifier.Operation.ADD_SCALAR);
        } else {
            EntityUtils.removeUniqueAttribute(entity, "es_fire_resistance_slow", Attribute.GENERIC_MOVEMENT_SPEED);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "es_fire_resistance_slow", Attribute.GENERIC_MOVEMENT_SPEED);
    }
}