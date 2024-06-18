package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class Strength extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
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
    public Strength(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.strength.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.strength.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.strength.incompatible_custom_enchantments"));

        this.damageBase = config.getDouble("enchantment_configuration.strength.damage_base");
        this.damageLv = config.getDouble("enchantment_configuration.strength.damage_lv");
        this.percentileIncrease = config.getBoolean("enchantment_configuration.strength.percentile_increase");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.strength.icon", createIcon(Material.BLAZE_POWDER));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.strength.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.strength.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.strength.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.strength";
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
        return config.getInt("enchantment_configuration.strength.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.strength.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.strength.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.strength.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.strength.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.strength.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.strength.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.strength.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.strength.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.strength.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-barbarian";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double damageBase;
    private final double damageLv;
    private final boolean percentileIncrease;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_strength", Attribute.GENERIC_ATTACK_DAMAGE);
            return;
        }

        double damageBoost = damageBase + ((level - 1) * damageLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, STRENGTH_UUID, "es_strength", Attribute.GENERIC_ATTACK_DAMAGE, damageBoost,
                percentileIncrease ? AttributeModifier.Operation.ADD_SCALAR : AttributeModifier.Operation.ADD_NUMBER);
    }

    private final UUID STRENGTH_UUID = UUID.fromString("50795496-8f0e-4318-9f77-3e11b7e342c5");

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "es_strength", Attribute.GENERIC_ATTACK_DAMAGE);
    }
}