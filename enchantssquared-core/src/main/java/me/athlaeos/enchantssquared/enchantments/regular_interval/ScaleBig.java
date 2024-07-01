package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.AttributeEnchantment;
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
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ScaleBig extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, AttributeEnchantment {
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
    public ScaleBig(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.scale2.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.scale2.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.scale2.incompatible_custom_enchantments"));

        this.scaleBase = config.getDouble("enchantment_configuration.scale2.scale_base");
        this.scaleLv = config.getDouble("enchantment_configuration.scale2.scale_lv");
        this.affectsReach = config.getBoolean("enchantment_configuration.scale2.affects_reach");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.scale2.icon", createIcon(Material.SLIME_BLOCK));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.scale2.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.scale2.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.scale2.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.scale2";
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
        return config.getInt("enchantment_configuration.scale2.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.scale2.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.scale2.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.scale2.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.scale2.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.scale2.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.scale2.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.scale2.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.scale2.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.scale2.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-scale2";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double scaleBase;
    private final double scaleLv;
    private final boolean affectsReach;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_scale2_scale", Attribute.GENERIC_SCALE);
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_scale2_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_scale2_entity_reach", Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
            return;
        }

        double scaleBoost = scaleBase + ((level - 1) * scaleLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, SCALE_UUID, "es_scale2_scale", Attribute.GENERIC_SCALE, scaleBoost, AttributeModifier.Operation.ADD_SCALAR);
        if (affectsReach){
            EntityUtils.addUniqueAttribute((LivingEntity) e, BLOCK_REACH_UUID, "es_scale2_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE, scaleBoost, AttributeModifier.Operation.ADD_SCALAR);
            EntityUtils.addUniqueAttribute((LivingEntity) e, ENTITY_REACH_UUID, "es_scale2_entity_reach", Attribute.PLAYER_ENTITY_INTERACTION_RANGE, scaleBoost, AttributeModifier.Operation.ADD_SCALAR);
        } else {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_scale2_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_scale2_entity_reach", Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
        }
    }

    public static final UUID SCALE_UUID = UUID.fromString("66e1d3c3-521a-4787-a813-57c89b540b5e");
    public static final UUID BLOCK_REACH_UUID = UUID.fromString("723fea42-8ce4-4b9b-9890-e473eb3569b9");
    public static final UUID ENTITY_REACH_UUID = UUID.fromString("15175ff0-b522-4b69-9682-58367a5f24a9");

    @Override
    public void cleanAttribute(LivingEntity e) {
        EntityUtils.removeUniqueAttribute(e, "es_scale2_scale", Attribute.GENERIC_SCALE);
        EntityUtils.removeUniqueAttribute(e, "es_scale2_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
        EntityUtils.removeUniqueAttribute(e, "es_scale2_entity_reach", Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
    }
}