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

public class BlockReach extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, AttributeEnchantment {
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
    public BlockReach(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.block_reach.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.block_reach.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.block_reach.incompatible_custom_enchantments"));

        this.blockReachBase = config.getDouble("enchantment_configuration.block_reach.block_reach_base");
        this.blockReachLv = config.getDouble("enchantment_configuration.block_reach.block_reach_lv");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.block_reach.icon", createIcon(Material.PISTON));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.block_reach.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.block_reach.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.block_reach.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.block_reach";
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
        return config.getInt("enchantment_configuration.block_reach.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.block_reach.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.block_reach.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.block_reach.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.block_reach.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.block_reach.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.block_reach.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.block_reach.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.block_reach.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.block_reach.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-block-reach";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double blockReachBase;
    private final double blockReachLv;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
            return;
        }

        double stepHeightBoost = blockReachBase + ((level - 1) * blockReachLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, STEP_HEIGHT_UUID, "es_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE, stepHeightBoost, AttributeModifier.Operation.ADD_NUMBER);
    }

    public static final UUID STEP_HEIGHT_UUID = UUID.fromString("c93dd786-f471-4930-8bf1-401dcc224514");

    @Override
    public void cleanAttribute(LivingEntity e) {
        EntityUtils.removeUniqueAttribute(e, "es_block_reach", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
    }
}