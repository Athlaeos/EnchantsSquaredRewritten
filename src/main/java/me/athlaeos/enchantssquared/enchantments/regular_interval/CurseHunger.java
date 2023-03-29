package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class CurseHunger extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment {
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
    public CurseHunger(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.curse_hunger.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_hunger.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_hunger.incompatible_custom_enchantments"));

        this.hungerPerSecondBase = config.getDouble("enchantment_configuration.curse_hunger.hunger_degeneration_base");
        this.hungerPerSecondLv = config.getDouble("enchantment_configuration.curse_hunger.hunger_degeneration_lv");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.curse_hunger.icon", createIcon(Material.ROTTEN_FLESH));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.curse_hunger.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.curse_hunger.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.curse_hunger.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.curse_hunger";
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
        return config.getInt("enchantment_configuration.curse_hunger.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.curse_hunger.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.curse_hunger.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.curse_hunger.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.curse_hunger.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.curse_hunger.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.curse_hunger.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.curse_hunger.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_hunger.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_hunger.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-curse-hunger";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double hungerPerSecondBase;
    private final double hungerPerSecondLv;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof Player)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) return;
        Player p = (Player) e;

        int hungerDegenerated = Utils.excessChance(hungerPerSecondBase + (level - 1) * hungerPerSecondLv);

        if (p.getSaturation() > 0){
            p.setSaturation(Math.max(0, p.getSaturation() - hungerDegenerated));
        } else {
            p.setFoodLevel(Math.max(0, p.getFoodLevel() - hungerDegenerated));
        }
    }
}
