package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashSet;

public class Vigorous extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
    private final boolean cleanup;
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
    public Vigorous(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.vigorous.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.vigorous.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.vigorous.incompatible_custom_enchantments"));

        this.healthBase = config.getDouble("enchantment_configuration.vigorous.health_base");
        this.healthLv = config.getDouble("enchantment_configuration.vigorous.health_lv");
        this.percentileIncrease = config.getBoolean("enchantment_configuration.vigorous.percentile_increase");

        this.cleanup = config.getBoolean("clean_old_enchantments");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.vigorous.icon", createIcon(Material.GLISTERING_MELON_SLICE));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.vigorous.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.vigorous.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.vigorous.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.vigorous";
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
        return config.getInt("enchantment_configuration.vigorous.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.vigorous.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.vigorous.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.vigorous.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.vigorous.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.vigorous.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.vigorous.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.vigorous.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.vigorous.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.vigorous.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-vigorous";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double healthBase;
    private final double healthLv;
    private final boolean percentileIncrease;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_vigorous", Attribute.GENERIC_MAX_HEALTH);
            return;
        }

        double healthBoost = healthBase + ((level - 1) * healthLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, "es_vigorous", Attribute.GENERIC_MAX_HEALTH, healthBoost,
                percentileIncrease ? AttributeModifier.Operation.ADD_SCALAR : AttributeModifier.Operation.ADD_NUMBER);

        if (cleanup && e instanceof Player) stripEnchantmentAttributesFromAllEquipment((Player) e);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "es_vigorous", Attribute.GENERIC_MAX_HEALTH);
    }

    private void stripEnchantmentAttributesFromAllEquipment(Player p){
        EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p);
        if (equipment.getHelmetEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getHelmet());
        if (equipment.getChestplateEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getChestplate());
        if (equipment.getLeggingsEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getLeggings());
        if (equipment.getBootsEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getBoots());
        if (equipment.getMainHandEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getMainHand());
        if (equipment.getOffHandEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getOffHand());
    }

    private void stripEnchantmentAttributesFromItem(ItemStack i){
        if (ItemUtils.isAirOrNull(i)) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        if (meta.getAttributeModifiers(Attribute.GENERIC_MAX_HEALTH) == null) return;
        meta.removeAttributeModifier(Attribute.GENERIC_MAX_HEALTH);
        i.setItemMeta(meta);
    }
}