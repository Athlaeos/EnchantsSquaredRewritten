package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.Levels1IfPresent;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class Rejuvenation extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment {
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
    public Rejuvenation(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.rejuvenation.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.rejuvenation.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.rejuvenation.incompatible_custom_enchantments"));

        this.durabilityPerSecondBase = config.getDouble("enchantment_configuration.rejuvenation.durability_regeneration_base");
        this.durabilityPerSecondLv = config.getDouble("enchantment_configuration.rejuvenation.durability_regeneration_lv");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.rejuvenation.icon", createIcon(Material.EMERALD));
    }

    private final LevelService levelService = new Levels1IfPresent(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.rejuvenation.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.rejuvenation.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.rejuvenation.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.rejuvenation";
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
        return config.getInt("enchantment_configuration.rejuvenation.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.rejuvenation.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.rejuvenation.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.rejuvenation.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.rejuvenation.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.rejuvenation.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.rejuvenation.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.rejuvenation.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.rejuvenation.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.rejuvenation.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-rejuvenation";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double durabilityPerSecondBase;
    private final double durabilityPerSecondLv;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof Player)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) return;

        EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment((Player) e);
        Map<ItemStack, Map<CustomEnchant, Integer>> iterable = cachedEquipment.getIterableWithEnchantments(true);
        for (ItemStack i : iterable.keySet()){
            if (i.getType().getMaxDurability() > 0 && i.getItemMeta() instanceof Damageable){
                int rejuvLevel = iterable.getOrDefault(i, new HashMap<>()).getOrDefault(this, 0);
                if (rejuvLevel > 0){
                    int durabilityToRepair = Utils.excessChance(durabilityPerSecondBase + (durabilityPerSecondLv * (rejuvLevel - 1)));
                    ItemUtils.damageItem((Player) e, i, -durabilityToRepair);
                }
            }
        }
    }
}
